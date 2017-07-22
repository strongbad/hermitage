package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import org.springframework.transaction.*;

import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

@EverythingIsNonnullByDefault
public interface TransactionStep<T> {

  Callable<T> prepare(JdbcStatementExecutor statementExecutor);

  default boolean isTerminalOperation() {
    return false;
  }

  static TransactionStep<TransactionStatus> start(TransactionDefinition transactionDefinition) {
    return new Start(transactionDefinition);
  }

  static TransactionStep<SqlResult.ResultSet> select(SqlStatement sqlStatement) {
    return new Select(sqlStatement);
  }

  static TransactionStep<SqlResult> update(SqlStatement sqlStatement) {
    return new Update(sqlStatement);
  }

  static TransactionStep<SqlResult> insert(SqlStatement sqlStatement) {
    return update(sqlStatement);
  }

  static TransactionStep<SqlResult> commit(TransactionStatus transactionStatus) {
    return new Commit(transactionStatus);
  }

  static TransactionStep<SqlResult> rollback(TransactionStatus transactionStatus) {
    return new Rollback(transactionStatus);
  }

  static TransactionStep<Void> close() {
    return new Close();
  }

  class Start implements TransactionStep<TransactionStatus> {

    private final TransactionDefinition transactionDefinition;

    Start(TransactionDefinition transactionDefinition) {
      this.transactionDefinition = requireNonNull(transactionDefinition);
    }

    @Override
    public Callable<TransactionStatus> prepare(JdbcStatementExecutor statementExecutor) {
      return () -> statementExecutor.start(transactionDefinition);
    }

  }

  class Select implements TransactionStep<SqlResult.ResultSet> {

    private final SqlStatement sqlStatement;

    Select(SqlStatement sqlStatement) {
      this.sqlStatement = requireNonNull(sqlStatement);
    }

    @Override
    public Callable<SqlResult.ResultSet> prepare(JdbcStatementExecutor statementExecutor) {
      return () -> statementExecutor.select(sqlStatement);
    }

  }

  class Update implements TransactionStep<SqlResult> {

    private final SqlStatement sqlStatement;

    Update(SqlStatement sqlStatement) {
      this.sqlStatement = requireNonNull(sqlStatement);
    }

    @Override
    public Callable<SqlResult> prepare(JdbcStatementExecutor statementExecutor) {
      return () -> statementExecutor.update(sqlStatement);
    }

  }

  class Commit implements TransactionStep<SqlResult> {

    private final TransactionStatus transactionStatus;

    Commit(TransactionStatus transactionStatus) {
      this.transactionStatus = requireNonNull(transactionStatus);
    }

    @Override
    public Callable<SqlResult> prepare(JdbcStatementExecutor statementExecutor) {
      return () -> statementExecutor.commit(transactionStatus);
    }

    @Override
    public boolean isTerminalOperation() {
      return true;
    }

  }

  class Rollback implements TransactionStep<SqlResult> {

    private final TransactionStatus transactionStatus;

    Rollback(TransactionStatus transactionStatus) {
      this.transactionStatus = requireNonNull(transactionStatus);
    }

    @Override
    public Callable<SqlResult> prepare(JdbcStatementExecutor statementExecutor) {
      return () -> statementExecutor.rollback(transactionStatus);
    }

    @Override
    public boolean isTerminalOperation() {
      return true;
    }

  }

  class Close implements TransactionStep<Void> {

    Close() {}

    @Override
    public Callable<Void> prepare(JdbcStatementExecutor statementExecutor) {
      return () -> null;
    }

    @Override
    public boolean isTerminalOperation() {
      return true;
    }

  }

}
