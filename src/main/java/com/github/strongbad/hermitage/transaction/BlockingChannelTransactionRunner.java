package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.*;

import static java.util.Objects.requireNonNull;

@Immutable
@EverythingIsNonnullByDefault
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BlockingChannelTransactionRunner implements TransactionRunner {

  private final JdbcStatementExecutor statementExecutor;
  private final TransactionChannel transactionChannel;
  private final ExecutorService executorService;

  public BlockingChannelTransactionRunner(JdbcStatementExecutor statementExecutor, TransactionChannel transactionChannel) {
    this.statementExecutor = requireNonNull(statementExecutor);
    this.transactionChannel = requireNonNull(transactionChannel);
    this.executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public TransactionChannel getChannel() {
    return transactionChannel;
  }

  @Override
  public void run() {
    try {
      blockingLoop();
    }
    catch (InterruptedException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void blockingLoop() throws InterruptedException {
    TransactionStep<?> step;
    do {
      step = transactionChannel.takeStep();
      Callable<?> callable = step.prepare(statementExecutor);
      Future<?> result = executorService.submit(callable);
      transactionChannel.putResult(result);
    }
    while (!step.isTerminalOperation());
  }

}
