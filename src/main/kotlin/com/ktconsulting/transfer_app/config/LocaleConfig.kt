package com.ktconsulting.transfer_app.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.Locale

@Configuration
class LocaleConfig {

    @Bean
    fun messageSource(): MessageSource {
        val source = ReloadableResourceBundleMessageSource()
        source.setBasename("classpath:messages")
        source.setDefaultEncoding("UTF-8")
        source.setDefaultLocale(Locale.FRENCH)
        return source
    }

    @Bean
    fun localeResolver(): LocaleResolver {
        val resolver = AcceptHeaderLocaleResolver()
        resolver.setDefaultLocale(Locale.FRENCH)
        resolver.supportedLocales = listOf(Locale.FRENCH, Locale("ar"))
        return resolver
    }
}
