package com.vega.armazem.config;

import com.vega.armazem.domain.TipoIngrediente;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Parâmetros de URL (?tipo=secos) usam a mesma leitura flexível do JSON. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, TipoIngrediente.class, TipoIngrediente::of);
    }
}
