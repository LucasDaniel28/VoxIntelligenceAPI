package com.voxintelligence.api.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxintelligence.api.audit.model.AuditEvent;
import com.voxintelligence.api.audit.repository.AuditEventRepository;
import com.voxintelligence.api.budget.model.MonthlyBudget;
import com.voxintelligence.api.budget.repository.MonthlyBudgetRepository;
import com.voxintelligence.api.transaction.model.FinancialTransaction;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PostgreSqlPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("vox_test")
            .withUsername("vox")
            .withPassword("vox");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Autowired
    private MonthlyBudgetRepository budgetRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Test
    void shouldApplyAllMigrationsAndValidateTheJpaSchema() {
        assertThat(flyway.info().applied()).hasSize(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' "
                        + "AND table_name IN ('financial_transactions', 'monthly_budgets', 'audit_events')",
                Integer.class)).isEqualTo(3);
    }

    @Test
    void shouldPersistAndQueryFinancialTransactions() {
        FinancialTransaction transaction = new FinancialTransaction(
                UUID.randomUUID(), TransactionType.EXPENSE, new BigDecimal("42.50"), "BRL",
                "Almoço", "Restaurante", TransactionCategory.FOOD, "RESTAURANT",
                LocalDate.of(2026, 11, 3));

        transactionRepository.saveAndFlush(transaction);

        assertThat(transactionRepository.findById(transaction.getId())).isPresent();
        assertThat(transactionRepository.sumByTypeAndPeriod(
                TransactionType.EXPENSE, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30))).isEqualByComparingTo("42.50");
        assertThat(transaction.getCreatedAt()).isNotNull();
        assertThat(transaction.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldPersistMonthlyBudgetsAndAuditEvents() {
        MonthlyBudget budget = new MonthlyBudget(
                UUID.randomUUID(), TransactionCategory.FOOD, 2026, 11, new BigDecimal("800.00"));
        budgetRepository.saveAndFlush(budget);

        AuditEvent event = new AuditEvent(UUID.randomUUID(), "MONTHLY_BUDGET", budget.getId(),
                "CREATE", null, "{\"limitAmount\":800.00}", Instant.now());
        auditEventRepository.saveAndFlush(event);

        assertThat(budgetRepository.findByCategoryAndYearAndMonth(
                TransactionCategory.FOOD, 2026, 11)).isPresent();
        assertThat(auditEventRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                "MONTHLY_BUDGET", budget.getId())).hasSize(1);
    }
}
