package com.github.strongbad.hermitage.transaction;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

import static com.github.strongbad.hermitage.util.InterruptibleSupplier.interruptible;

@EverythingIsNonnullByDefault
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
final class TransactionChannel {

  private final BlockingQueue<TransactionStep<?>> steps;
  private final BlockingQueue<Future<?>> results;

  private TransactionChannel() {
    this.steps = new LinkedBlockingQueue<>();
    this.results = new LinkedBlockingQueue<>();
  }

  <T> Future<T> putStep(TransactionStep<T> step) {
      return interruptible(() -> {
        steps.put(step);
        @SuppressWarnings("unchecked")
        Future<T> result = (Future<T>) results.take();
        return result;
      });
  }

  TransactionStep<?> takeStep() {
    return interruptible(steps::take);
  }

  void putResult(Future<?> future) {
    interruptible(() -> {
      results.put(future);
      return VOID;
    });
  }

  private static final Object VOID = new Object();

}
