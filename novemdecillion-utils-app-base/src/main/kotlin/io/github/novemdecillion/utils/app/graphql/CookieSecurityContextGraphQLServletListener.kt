package io.github.novemdecillion.utils.app.graphql

import graphql.kickstart.servlet.core.GraphQLServletListener
import io.github.novemdecillion.utils.app.security.CookieSecurityContextRepository
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CookieSecurityContextGraphQLServletListener(private val securityContextRepository: CookieSecurityContextRepository)
  : GraphQLServletListener {

  override fun onRequest(request: HttpServletRequest?, response: HttpServletResponse?): GraphQLServletListener.RequestCallback {
    return object : GraphQLServletListener.RequestCallback {
      override fun beforeFlush(request: HttpServletRequest, response: HttpServletResponse) {
        val context = SecurityContextHolder.getContext()
        securityContextRepository.asyncSaveContext(context, request, response)
      }
    }
  }
}