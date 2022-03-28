package io.github.novemdecillion.utils.app.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.novemdecillion.utils.slf4j.logger
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.codec.Hex
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.SecurityContextRepository
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@ConfigurationProperties(prefix = "novemdecillion.app.token")
@ConstructorBinding
data class CookieSecurityContextProperties(
  val secret: String,
  val salt: String,
  val duration: Long
)

class CookieSecurityContextRepository(
  private val objectMapper: ObjectMapper,
  private val userDetailsService: UserDetailsService,
  properties: CookieSecurityContextProperties,
  applicationContext: ApplicationContext
) : SecurityContextRepository {

  data class CachePayload(val account: UserDetails, val expire: LocalDateTime) {
    fun toCookiePayload(): CookiePayload {
      return CookiePayload(account.username, expire)
    }
  }

  data class CookiePayload(val username: String, val expire: LocalDateTime)

  companion object {
    val NO_UPDATE_COOKIE = "${CookieSecurityContextRepository::class.java.simpleName}-NO_UPDATE_COOKIE"
    private const val SAME_SITE_STRICT = "Strict"
    private const val durationMinutes = 30L
    private val log = logger()
  }

  private val cookieName = "${applicationContext.id}.COGITO_ERGO_SUM"
  private val usernameToAccountCache = ConcurrentHashMap<String, CachePayload>()

  private val salt = Hex.encode(properties.salt.toByteArray()).concatToString()
  private val encryptor: TextEncryptor = Encryptors.delux(properties.secret, salt)


  override fun loadContext(requestResponseHolder: HttpRequestResponseHolder): SecurityContext {
    val context = SecurityContextHolder.createEmptyContext()
    loadCookiePayload(requestResponseHolder.request)
      ?.let { cookiePayload ->
        usernameToAccountCache[cookiePayload.username]
          ?: run {
            try {
              CachePayload(userDetailsService.loadUserByUsername(cookiePayload.username), cookiePayload.expire)
            } catch (ex: Exception) {
              null
            }
          }
      }
      ?.also {
        context.authentication =
          UsernamePasswordAuthenticationToken(it.account, StringUtils.EMPTY, it.account.authorities)
      }
    if (log.isDebugEnabled) {
      log.debug(
        "loadContext: user={}, account={}) ",
        loadCookiePayload(requestResponseHolder.request)?.username,
        context.authentication?.principal
      )
    }
    return context
  }

  /**
   * 非同期の場合は、loadContextの後で、リクエストの処理が行われる前に呼び出されてしまう。
   *
   * @see https://github.com/spring-projects/spring-security/issues/9342
   */
  override fun saveContext(context: SecurityContext, request: HttpServletRequest, response: HttpServletResponse) {
    if (log.isDebugEnabled) {
      log.debug(
        "saveContext: user={}, account={}) ",
        loadCookiePayload(request)?.username,
        context.authentication?.principal
      )
    }

    if (!request.isAsyncStarted) {
      execSaveContext(context, request, response)
    }
  }

  fun asyncSaveContext(context: SecurityContext, request: HttpServletRequest, response: HttpServletResponse) {
    if (log.isDebugEnabled) {
      log.debug(
        "asyncSaveContext: user={}, account={}) ",
        loadCookiePayload(request)?.username,
        context.authentication?.principal
      )
    }
    execSaveContext(context, request, response)
  }

  private fun execSaveContext(context: SecurityContext, request: HttpServletRequest, response: HttpServletResponse) {
    if (request.getAttribute(NO_UPDATE_COOKIE) == true) {
      return
    }

    (context.authentication?.principal as? UserDetails)
      ?.let { account ->
        val now = LocalDateTime.now()

        // 期限切れのキャッシュを削除する
        usernameToAccountCache.entries.removeIf { (_, cache) -> cache.expire < now }

        val cachePayload = CachePayload(account, now.plusMinutes(durationMinutes))
        usernameToAccountCache[account.username] = cachePayload
        cachePayload.toCookiePayload()
      }
      ?.let { objectMapper.writeValueAsString(it) }
      ?.let { encryptor.encrypt(it) }
      .also {
        val cookie = ResponseCookie.from(cookieName, it.orEmpty())
          .secure(true).httpOnly(true).sameSite(SAME_SITE_STRICT)
          .build()
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString())
      }
      ?: run {
        // キャッシュから削除
        loadCookiePayload(request)?.also { usernameToAccountCache.remove(it.username) }
      }
  }

  override fun containsContext(request: HttpServletRequest): Boolean {
    return loadCookiePayload(request) != null
  }

  private fun loadCookiePayload(request: HttpServletRequest): CookiePayload? {
    return try {
      request.cookies
        ?.firstOrNull { it.name == cookieName }
        ?.let { it.value.ifEmpty { null } }
        ?.let { encryptor.decrypt(it) }
        ?.let { objectMapper.readValue(it, CookiePayload::class.java) }
        ?.let {
          if (LocalDateTime.now() < it.expire) {
            it
          } else {
            log.debug("Cookieは期限切れです。")
            usernameToAccountCache.remove(it.username)
            null
          }
        }
    } catch (ex: Exception) {
      log.warn("Cookieからの取得に失敗しました。", ex)
      null
    }
  }
}
