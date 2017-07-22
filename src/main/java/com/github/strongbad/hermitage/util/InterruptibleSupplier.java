package com.github.strongbad.hermitage.util;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;

import java.util.concurrent.*;

@FunctionalInterface
@EverythingIsNonnullByDefault
public interface InterruptibleSupplier<T> {

  T get() throws InterruptedException, ExecutionException, TimeoutException;

  static <T> T interruptible(InterruptibleSupplier<T> supplier) {
    try {
      return supplier.get();
    }
    catch (InterruptedException|ExecutionException|TimeoutException exception) {
      throw new RuntimeException(exception);
    }
  }

}
