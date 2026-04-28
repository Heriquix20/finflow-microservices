package com.finflow.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void shouldBuildCorsFilterWithConfiguredOrigins() {
        CorsConfig config = new CorsConfig(new String[]{"http://localhost:8080", "http://localhost:3000"});

        CorsWebFilter filter = config.corsWebFilter();

        assertThat(filter).isNotNull();
    }
}
