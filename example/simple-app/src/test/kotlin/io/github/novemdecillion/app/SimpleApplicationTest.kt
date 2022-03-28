package io.github.novemdecillion.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.graphql.spring.boot.test.GraphQLTestTemplate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SimpleApplicationTest(val testRestTemplate: GraphQLTestTemplate, val objectMapper: ObjectMapper) {
  data class LoginInput(val username: String, val password: String)

  @Test
  fun `アクセス拒否`() {
    var response = testRestTemplate.postForResource("/graphql/users.graphql")
    Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    response.assertThatListOfErrors().anyMatch {
      it.message == "アクセスが拒否されました"
    }

    val variable =objectMapper.valueToTree<ObjectNode>(LoginInput("admin", "password123"))
    response = testRestTemplate.perform("/graphql/login.graphql", variable)
    response.assertThatNoErrorsArePresent()

    val cookies = response.rawResponse.headers[HttpHeaders.SET_COOKIE] as List<String>
    Assertions.assertThat(cookies).size().isEqualTo(1)

    response = testRestTemplate.withAdditionalHeader(HttpHeaders.COOKIE, cookies[0]).postForResource("/graphql/users.graphql")
    response.assertThatNoErrorsArePresent()
  }
}