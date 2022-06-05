package io.lonmstalker.scriptservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache

@EnableReactiveMethodSecurity
@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    @Bean
    fun securityChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain = http
        .httpBasic().disable()
        .cors().disable()
        .csrf().disable()
        .logout().disable()
        .formLogin().disable()
        .requestCache { it.requestCache(NoOpServerRequestCache.getInstance()) }
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange {
            it.pathMatchers("/**").permitAll()
                .anyExchange().authenticated()
        }
        .exceptionHandling().authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
        .and()
        .build()

}