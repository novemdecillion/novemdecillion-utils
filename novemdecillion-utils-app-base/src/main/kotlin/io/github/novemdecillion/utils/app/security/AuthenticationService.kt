package io.github.novemdecillion.utils.app.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationService(private val authenticationManager: AuthenticationManager) {

  fun login(username: String, password: String)
    : Authentication {

    val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
    val securityContext = SecurityContextHolder.createEmptyContext()
    securityContext.authentication = authentication
    SecurityContextHolder.setContext(securityContext)
    return authentication
  }

  fun logout() {
    SecurityContextHolder.clearContext()
  }

  fun ping(request: HttpServletRequest, response: HttpServletResponse) {
    request.setAttribute(CookieSecurityContextRepository.NO_UPDATE_COOKIE, true)
  }

}