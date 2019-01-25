package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.Optional;
import java.util.concurrent.Future;

import static com.github.strongbad.hermitage.transaction.StepResult.futureResult;
import static java.util.Objects.requireNonNull;

@EverythingIsNonnullByDefault
public class Transaction {

  private final TransactionChannel transactionChannel;
  private final Future<?> runnerState;
  private final TransactionStatus transactionStatus;

  static Transaction create(TransactionChannel transactionChannel, Future<?> runnerState, TransactionDefinition transactionDefinition) {
    return new Transaction(transactionChannel, runnerState, transactionDefinition);
  }

  private Transaction(TransactionChannel transactionChannel, Future<?> runnerState, TransactionDefinition transactionDefinition) {
    this.transactionChannel = requireNonNull(transactionChannel);
    this.runnerState = requireNonNull(runnerState);
    this.transactionStatus = start(transactionDefinition);
  }

  public StepResult<SqlResult.ResultSet> select(SqlStatement sqlStatement) {
    return futureResult(transactionChannel.putStep(TransactionStep.select(sqlStatement)));
  }

  public StepResult<SqlResult> update(SqlStatement sqlStatement) {
    return futureResult(transactionChannel.putStep(TransactionStep.update(sqlStatement)));
  }

  public StepResult<SqlResult> insert(SqlStatement sqlStatement) {
    return futureResult(transactionChannel.putStep(TransactionStep.insert(sqlStatement)));
  }

  public StepResult<SqlResult> commit() {
    return futureResult(transactionChannel.putStep(TransactionStep.commit(transactionStatus)));
  }

  public StepResult<SqlResult> rollback() {
    return futureResult(transactionChannel.putStep(TransactionStep.rollback(transactionStatus)));
  }

  Optional<StepResult<?>> close() {
    if (!runnerState.isDone() && !runnerState.isCancelled()) {
      Future<?> result = transactionChannel.putStep(TransactionStep.rollback(transactionStatus));
      return Optional.of(StepResult.futureResult(result));
    }
    return Optional.empty();
  }

  private TransactionStatus start(TransactionDefinition transactionDefinition) {
    return futureResult(transactionChannel.putStep(TransactionStep.start(transactionDefinition))).waitForCompletion();
  }

}
