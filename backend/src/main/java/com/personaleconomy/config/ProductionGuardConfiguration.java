package com.personaleconomy.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Prevents the local development schema/identity adapter from being deployed as production. */
@Configuration(proxyBeanMethods = false)
@Profile("prod")
public class ProductionGuardConfiguration {
    @Bean public static BeanFactoryPostProcessor blockUnfinishedProduction() {
        return beanFactory -> {
            throw new IllegalStateException("Production authentication and schema review are pending; use dev for local development");
        };
    }
}
