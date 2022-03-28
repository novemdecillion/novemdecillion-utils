package io.github.novemdecillion.app.adapter.api

import graphql.kickstart.tools.GraphQLMutationResolver
import graphql.kickstart.tools.GraphQLQueryResolver
import io.github.novemdecillion.app.adapter.repository.InMemoryUserRepository
import io.github.novemdecillion.app.domain.ROLE_ADMIN
import io.github.novemdecillion.app.domain.ROLE_USER
import io.github.novemdecillion.utils.app.security.AuthenticationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component

@Component
class UserApi(private val authenticationService: AuthenticationService,
              private val userRepository: InMemoryUserRepository) : GraphQLQueryResolver, GraphQLMutationResolver {
  companion object {
    const val AUTHENTICATED = "isAuthenticated()"
    const val ONLY_ADMIN = "hasAuthority('${ROLE_ADMIN}')"
    const val ONLY_USER = "hasAuthority('${ROLE_USER}')"
  }

  fun login(mailAddress: String, password: String): User? {
    return authenticationService.login(mailAddress, password).principal as? User
  }

  @PreAuthorize(ONLY_ADMIN)
  fun users(): List<User> {
    return userRepository.selectAll()
  }
}