import com.nimbusds.jose.crypto.DirectEncrypter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@EnableReactiveMethodSecurity
@Configuration(proxyBeanMethods = false)
class SecurityConfig {
    private final val secretKey: SecretKey

    init {
        val keyGenerator = KeyGenerator.getInstance("AES")
        secretKey = object : SecretKey {
            override fun getAlgorithm(): String = "AES"

            override fun getFormat(): String = "A128CBC_HS256"

            override fun getEncoded(): ByteArray = "841D8A6C80CBA4FCAD32D5367C18C53B".toByteArray()
        }
        LOGGER.info("secret is ${secretKey.encoded}")
    }

    @Bean
    fun securityChain(
        http: ServerHttpSecurity, authenticationManager: ReactiveAuthenticationManager
    ): SecurityWebFilterChain = http
        .httpBasic().disable()
        .cors().disable()
        .csrf().disable()
        .logout().disable()
        .formLogin().disable()
        .requestCache { it.requestCache(NoOpServerRequestCache.getInstance()) }
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange {
            it.pathMatchers("/internal/**").permitAll()
                .anyExchange().authenticated()
        }
        .exceptionHandling().authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
        .and()
        .addFilterAt(this.getAuthFilter(authenticationManager), SecurityWebFiltersOrder.AUTHENTICATION)
        .build()

    @Bean
    fun reactiveAuthManager(jwtDecoder: ReactiveJwtDecoder): ReactiveAuthenticationManager {
        val authenticationManager = JwtReactiveAuthenticationManager(jwtDecoder)
        authenticationManager.setJwtAuthenticationConverter(ReactiveJwtAuthenticationConverter())
        return authenticationManager
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder.withSecretKey(this.secretKey).build()

    @Bean
    fun jwtEncoder() = DirectEncrypter("841D8A6C80CBA4FCAD32D5367C18C53B".toByteArray())

    // if we don't want call filter twice
    private fun getAuthFilter(manager: ReactiveAuthenticationManager): AuthenticationWebFilter {
        val authenticationWebFilter = AuthenticationWebFilter(manager)
        authenticationWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        authenticationWebFilter.setServerAuthenticationConverter(ServerBearerTokenAuthenticationConverter())
        // for internal tokens
        authenticationWebFilter.setRequiresAuthenticationMatcher(
            NegatedServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers("/internal/**"))
        )
        return authenticationWebFilter
    }

    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(this::class.java)
    }

}