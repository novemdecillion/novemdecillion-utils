package io.github.novemdecillion.utils.app.graphql

import graphql.kickstart.servlet.core.GraphQLServletListener
import io.github.novemdecillion.utils.app.security.CookieSecurityContextRepository
import org.dataloader.MappedBatchLoader
import org.dataloader.MappedBatchLoaderWithContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager

class GraphQLConfiguration {

  @ConditionalOnClass(GraphQLServletListener::class)
  @ConditionalOnBean(CookieSecurityContextRepository::class)
  @Bean
  fun cookieSecurityContextGraphQLServletListener(securityContextRepository: CookieSecurityContextRepository): CookieSecurityContextGraphQLServletListener {
    return CookieSecurityContextGraphQLServletListener(securityContextRepository)
  }

//  @Bean
//  fun graphQLServletContextBuilder(mappedBatchLoaders: Collection<MappedBatchLoader<*, *>>?,
//                                   mappedBatchLoaderWithContexts: Collection<MappedBatchLoaderWithContext<*, *>>?): GraphQLServletContextBuilder {
//    return GraphQLServletContextBuilder(mappedBatchLoaders, mappedBatchLoaderWithContexts)
//  }

  @Bean
  @ConditionalOnBean(PlatformTransactionManager::class)
  fun perRequestTransactionInstrumentation(transactionManager: PlatformTransactionManager): PerRequestTransactionInstrumentation {
    return PerRequestTransactionInstrumentation(transactionManager)
  }

}
