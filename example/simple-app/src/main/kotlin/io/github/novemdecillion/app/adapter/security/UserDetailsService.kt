package io.github.novemdecillion.app.adapter.security

import io.github.novemdecillion.app.adapter.repository.InMemoryUserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsService(val userRepository: InMemoryUserRepository) : org.springframework.security.core.userdetails.UserDetailsService {
  override fun loadUserByUsername(username: String): UserDetails {
    return userRepository.selectByUsername(username)
      ?: throw UsernameNotFoundException(username)
  }
}