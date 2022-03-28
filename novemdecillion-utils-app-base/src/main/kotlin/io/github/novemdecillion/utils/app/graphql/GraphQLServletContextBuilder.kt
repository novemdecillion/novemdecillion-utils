package io.github.novemdecillion.utils.app.graphql

import graphql.kickstart.execution.context.GraphQLContext
import graphql.kickstart.servlet.context.DefaultGraphQLServletContext
import graphql.kickstart.servlet.context.DefaultGraphQLServletContextBuilder
import graphql.schema.DataFetchingEnvironment
import org.dataloader.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass

class GraphQLServletContextBuilder(
  private val mappedBatchLoaders: Collection<MappedBatchLoader<*, *>>?,
  private val mappedBatchLoaderWithContexts: Collection<MappedBatchLoaderWithContext<*, *>>?) : DefaultGraphQLServletContextBuilder() {

  private val dataLoaderRegistry: DataLoaderRegistry = DataLoaderRegistry()
    .also { registry ->
      val options = DataLoaderOptions().setCachingEnabled(false).setBatchingEnabled(false)

      mappedBatchLoaders
        ?.forEach {
          val dataLoader = DataLoader.newMappedDataLoader(it, options)
          registry.register(it::class.java.simpleName, dataLoader)
        }
      mappedBatchLoaderWithContexts
        ?.forEach {
          val dataLoader = DataLoader.newMappedDataLoader(it, options)
          registry.register(it::class.java.simpleName, dataLoader)
        }
    }

  override fun build(request: HttpServletRequest, response: HttpServletResponse): GraphQLContext {
    return DefaultGraphQLServletContext.createServletContext().with(dataLoaderRegistry).with(request).with(response).build()
  }
}

fun <K, V, T: MappedBatchLoader<K, V>> DataFetchingEnvironment.dataLoader(key: KClass<T>): DataLoader<K, V> {
  return this.dataLoaderRegistry.getDataLoader(key.java.simpleName)
}
