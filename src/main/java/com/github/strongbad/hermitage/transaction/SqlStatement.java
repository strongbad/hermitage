package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@EverythingIsNonnullByDefault
public class SqlStatement {

  private final String sql;

  public static SqlStatement sql(String sql) {
    return new SqlStatement(sql);
  }

  private SqlStatement(String sql) {
    this.sql = sql;
  }

  String sql() {
    return sql;
  }

  public StepResult<SqlResult.ResultSet> select(Transaction transaction) {
    return transaction.select(this);
  }

  public StepResult<SqlResult> update(Transaction transaction) {
    return transaction.update(this);
  }

  public StepResult<SqlResult> insert(Transaction transaction) {
    return transaction.insert(this);
  }

  public StepResult<SqlResult> delete(Transaction transaction) {
    return update(transaction);
  }

}
