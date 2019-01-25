package com.github.strongbad.hermitage.transaction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.*;

import static com.github.strongbad.hermitage.util.InterruptibleSupplier.interruptible;
import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class StepResult<T> {

  private final Future<T> result;

  static <T> StepResult<T> futureResult(Future<T> result) {
    return new StepResult<>(result);
  }

  private StepResult(Future<T> result) {
    this.result = requireNonNull(result);
  }

  public T waitForCompletion() {
    return interruptible(() ->
      result.get(MS_TIMEOUT, TimeUnit.MILLISECONDS)
    );
  }

  public boolean isBlocking() {
    return !result.isDone() && !isDoneAfterTimeout();
  }

  public StepResult<T> into(WrappedValue<T> holder) {
    holder.result = this;
    return this;
  }

  public T into(UnwrappedValue<T> holder) {
    holder.result = this.waitForCompletion();
    return holder.get();
  }

  private boolean isDoneAfterTimeout() {
    try {
      result.get(MS_TIMEOUT, TimeUnit.MILLISECONDS);
      return result.isDone();
    }
    catch (InterruptedException|ExecutionException|TimeoutException exception) {
      return false;
    }
  }

  public static final class WrappedValue<T> {

    private StepResult<T> result;

    public static <T> WrappedValue<T> wrappedValueHolder() {
      return new WrappedValue<>();
    }

    private WrappedValue() {}

    public StepResult<T> get() {
      return result;
    }

  }

  public static final class UnwrappedValue<T> {

    private T result;

    public static <T> UnwrappedValue<T> unwrappedValueHolder() {
      return new UnwrappedValue<>();
    }

    private UnwrappedValue() {}

    public T get() {
      return result;
    }

  }

  private static final long MS_TIMEOUT = 500;

}
