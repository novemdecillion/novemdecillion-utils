package io.github.novemdecillion.utils.app.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository

@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConfigurationPropertiesScan
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    http.setSharedObject(
      SecurityContextRepository::class.java,
      applicationContext.getBean(SecurityContextRepository::class.java)
    )

    http
      .csrf().disable()
      .authorizeRequests()
      .antMatchers("/api").permitAll()
      .anyRequest().authenticated()
      .and()
      .logout().disable()
      // 無効化することで、SessionManagementFilterとRequestCacheAwareFilterが作られなくなる。
      .sessionManagement().disable()
  }

  @Bean
  @ConditionalOnMissingBean
  override fun authenticationManagerBean(): AuthenticationManager {
    return super.authenticationManagerBean()
  }

  @Bean
  @ConditionalOnMissingBean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Bean
  @ConditionalOnMissingBean
  fun securityContextRepository(
    objectMapper: ObjectMapper,
    userDetailsService: UserDetailsService,
    properties: CookieSecurityContextProperties,
    applicationContext: ApplicationContext
  ): CookieSecurityContextRepository {
    return CookieSecurityContextRepository(objectMapper, userDetailsService, properties, applicationContext)
  }

  @Bean
  @ConditionalOnMissingBean
  fun authenticationService(): AuthenticationService {
    return AuthenticationService(authenticationManagerBean())
  }

}

