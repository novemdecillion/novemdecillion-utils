package io.github.novemdecillion.app.adapter.repository

import io.github.novemdecillion.app.domain.ROLE_ADMIN
import io.github.novemdecillion.app.domain.ROLE_USER
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Repository

@Repository
class InMemoryUserRepository {
  companion object {
    private val users = listOf(
      User("admin", "password123", listOf(SimpleGrantedAuthority(ROLE_ADMIN))),
      User("user", "password123", listOf(SimpleGrantedAuthority(ROLE_USER)))
    )

    private val userMap = users.associateBy { it.username }
  }

  fun selectByUsername(username: String) = userMap[username]

  fun selectAll() = users

}