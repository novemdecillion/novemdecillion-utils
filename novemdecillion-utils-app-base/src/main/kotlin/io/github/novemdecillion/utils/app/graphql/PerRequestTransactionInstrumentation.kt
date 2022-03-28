package io.github.novemdecillion.utils.app.graphql

import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.SimpleInstrumentationContext
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters
import graphql.language.OperationDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

class PerRequestTransactionInstrumentation(private val transactionManager: PlatformTransactionManager) :
  SimpleInstrumentation() {

  companion object {
    val log: Logger = LoggerFactory.getLogger(PerRequestTransactionInstrumentation::class.java)
  }

  override fun beginExecuteOperation(parameters: InstrumentationExecuteOperationParameters): InstrumentationContext<ExecutionResult> {
    log.debug("transaction start")
    val tx = TransactionTemplate(transactionManager)
    if (OperationDefinition.Operation.QUERY == parameters.executionContext.operationDefinition.operation) {
      tx.isReadOnly = true
    }
    val status = transactionManager.getTransaction(tx)
    return SimpleInstrumentationContext.whenCompleted { result, ex ->
      if ((ex != null) || result.errors.isNotEmpty()) {
        status.setRollbackOnly()
      }

      when {
        status.isRollbackOnly -> {
          log.debug("transaction rollback ${parameters.executionContext.executionId}")
          transactionManager.rollback(status)
        }
        else -> {
          log.debug("transaction commit ${parameters.executionContext.executionId}")
          transactionManager.commit(status)
        }
      }
    }
  }
}