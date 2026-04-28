package com.finflow.reports.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.assertj.core.api.Assertions.assertThatCode;

class WebConfigTest {

    @Test
    void shouldRegisterCorsMappingsWithoutErrors() {
        WebConfig webConfig = new WebConfig(new String[]{"http://localhost:5173"});

        assertThatCode(() -> webConfig.addCorsMappings(new CorsRegistry()))
                .doesNotThrowAnyException();
    }
}
