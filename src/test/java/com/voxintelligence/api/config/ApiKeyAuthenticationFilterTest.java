package com.voxintelligence.api.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApiKeyAuthenticationFilterTest {

    @Test
    void shouldRejectRequestsWhenApiKeyIsNotConfigured() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(503, response.getStatus());
        verify(chain, org.mockito.Mockito.never())
                .doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectRequestsWithInvalidApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("chave-correta");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-KEY", "chave-incorreta");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldAllowRequestsWithValidApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("chave-correta");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-KEY", "chave-correta");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
