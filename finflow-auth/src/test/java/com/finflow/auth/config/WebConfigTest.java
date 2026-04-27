package com.finflow.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.assertj.core.api.Assertions.assertThatCode;

class WebConfigTest {

    @Test
    void shouldRegisterCorsMappings() {
        WebConfig config = new WebConfig(new String[]{"http://localhost:8080"});

        assertThatCode(() -> config.addCorsMappings(new CorsRegistry()))
                .doesNotThrowAnyException();
    }
}
