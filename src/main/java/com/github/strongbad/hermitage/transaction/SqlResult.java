package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import com.github.strongbad.hermitage.model.TestRow;

import javax.annotation.concurrent.Immutable;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Immutable
@EverythingIsNonnullByDefault
public abstract class SqlResult {

  static SqlResult empty() {
    return new Empty();
  }

  static SqlResult.ResultSet results(List<TestRow> results) {
    return new ResultSet(results);
  }

  static SqlResult.Exception exception(RuntimeException exception) {
    return new Exception(exception);
  }

  SqlResult() {}

  public Optional<RuntimeException> exception() {
    return Optional.empty();
  }

  public static final class Empty extends SqlResult {

    private Empty() {}

  }

  public static final class ResultSet extends SqlResult {

    private final List<TestRow> results;

    private ResultSet(List<TestRow> results) {
      this.results = requireNonNull(results);
    }

    public List<TestRow> getResults() {
      return results;
    }

  }

  public static final class Exception extends SqlResult {

    private final RuntimeException exception;

    private Exception(RuntimeException exception) {
      this.exception = requireNonNull(exception);
    }

    @Override
    public Optional<RuntimeException> exception() {
      return Optional.of(exception);
    }

  }

}
