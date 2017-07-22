package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import com.github.strongbad.hermitage.isolation.HermitageTransactionDefinition;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

import static java.util.Objects.requireNonNull;

@Component
@EverythingIsNonnullByDefault
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class TransactionFactory {

  private final ExecutorService executorService;
  private final ObjectFactory<TransactionRunner> transactionRunnerFactory;
  private final ObjectFactory<HermitageTransactionDefinition> transactionDefinitionFactory;
  private final Collection<Transaction> transactions;

  public TransactionFactory(
      ExecutorService executorService,
      ObjectFactory<TransactionRunner> transactionRunnerFactory,
      ObjectFactory<HermitageTransactionDefinition> transactionDefinitionFactory) {
    this.executorService = requireNonNull(executorService);
    this.transactionRunnerFactory = requireNonNull(transactionRunnerFactory);
    this.transactionDefinitionFactory = requireNonNull(transactionDefinitionFactory);
    this.transactions = new LinkedList<>();
  }

  public Transaction startTransaction() {
    TransactionRunner transactionRunner =transactionRunnerFactory.getObject();
    Future<?> runnerState = executorService.submit(transactionRunner);
    return createTransaction(transactionRunner.getChannel(), runnerState);
  }

  private Transaction createTransaction(TransactionChannel transactionChannel, Future<?> runnerState) {
    Transaction transaction = Transaction.create(transactionChannel, runnerState, transactionDefinitionFactory.getObject());
    transactions.add(transaction);
    return transaction;
  }

  public void closeTransactions() {
    transactions.stream()
      .map(Transaction::close)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .forEach(StepResult::waitForCompletion);
  }

}
