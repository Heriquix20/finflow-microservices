package com.finflow.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "minha-chave-super-secreta-minha-chave-super-secreta";

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
    }

    @Test
    void shouldSkipAuthenticationForPublicPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login").build()
        );

        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = chain(forwardedExchange);

        filter.filter(exchange, chain).block();

        assertThat(forwardedExchange.get()).isNotNull();
        assertThat(forwardedExchange.get().getRequest().getHeaders().containsKey("X-User-Id")).isFalse();
    }

    @Test
    void shouldSkipAuthenticationForRegisterPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/register").build()
        );

        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = chain(forwardedExchange);

        filter.filter(exchange, chain).block();

        assertThat(forwardedExchange.get()).isNotNull();
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/reports/balance").build()
        );

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/reports/balance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build()
        );

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAddUserHeaderAndContinueWhenTokenIsValid() {
        String token = generateToken("user-123");
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/reports/balance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, chain(forwardedExchange)).block();

        assertThat(forwardedExchange.get()).isNotNull();
        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Id"))
                .isEqualTo("user-123");
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenHasBlankSubject() {
        String token = generateToken(" ");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/reports/balance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldExposeExpectedOrder() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }

    @Test
    void shouldAllowOptionsRequestsWithoutAuthentication() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("/api/reports/balance").build()
        );

        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        filter.filter(exchange, chain(forwardedExchange)).block();

        assertThat(forwardedExchange.get()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    private GatewayFilterChain chain(AtomicReference<ServerWebExchange> forwardedExchange) {
        return exchange -> {
            forwardedExchange.set(exchange);
            return Mono.empty();
        };
    }

    private String generateToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(subject)
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}
