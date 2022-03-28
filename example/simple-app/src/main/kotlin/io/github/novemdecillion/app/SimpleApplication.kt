package io.github.novemdecillion.app

import io.github.novemdecillion.utils.app.graphql.GraphQLConfiguration
import io.github.novemdecillion.utils.app.security.SecurityConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class TestConfiguration {
  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return NoOpPasswordEncoder.getInstance()
  }
}


@SpringBootApplication//(scanBasePackageClasses = [SecurityConfiguration::class])
@ConfigurationPropertiesScan
@Import(SecurityConfiguration::class, GraphQLConfiguration::class)
class SimpleApplication {
}




fun main(args: Array<String>) {
  runApplication<SimpleApplication>(*args)
}
