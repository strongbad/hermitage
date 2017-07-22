package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import com.github.strongbad.hermitage.model.TestRow;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.*;

import javax.annotation.concurrent.Immutable;

import static java.util.Objects.requireNonNull;

@Component
@Immutable
@EverythingIsNonnullByDefault
class JdbcStatementExecutor {

  private final PlatformTransactionManager transactionManager;
  private final JdbcTemplate jdbcTemplate;

  JdbcStatementExecutor(PlatformTransactionManager transactionManager, JdbcTemplate jdbcTemplate) {
    this.transactionManager = requireNonNull(transactionManager);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  TransactionStatus start(TransactionDefinition transactionDefinition) {
    return transactionManager.getTransaction(transactionDefinition);
  }

  SqlResult commit(TransactionStatus transactionStatus) {
    try {
      transactionManager.commit(transactionStatus);
      return SqlResult.empty();
    }
    catch (TransactionException exception) {
      return SqlResult.exception(exception);
    }
  }

  SqlResult rollback(TransactionStatus transactionStatus) {
    transactionManager.rollback(transactionStatus);
    return SqlResult.empty();
  }

  SqlResult update(SqlStatement sqlStatement) throws DataAccessException {
    try {
      jdbcTemplate.update(sqlStatement.sql());
      return SqlResult.empty();
    }
    catch (DataAccessException exception) {
      return SqlResult.exception(exception);
    }
  }

  SqlResult.ResultSet select(SqlStatement sqlStatement) throws DataAccessException {
    return SqlResult.results(jdbcTemplate.query(sqlStatement.sql(), TestRow.mapper()));
  }

}
