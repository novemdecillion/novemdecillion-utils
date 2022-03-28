package io.github.novemdecillion.utils.app.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

inline fun <reified T: UserDetails> userDetailsFromSecurityContextHolder(): T {
  return SecurityContextHolder.getContext().authentication?.principal as T
}

inline fun <reified T: UserDetails> userDetailsFromSecurityContextHolderOrNull(): T? {
  return SecurityContextHolder.getContext().authentication?.principal as? T
}