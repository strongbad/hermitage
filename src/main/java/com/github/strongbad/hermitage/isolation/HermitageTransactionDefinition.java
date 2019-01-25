package com.github.strongbad.hermitage.isolation;

import com.github.strongbad.hermitage.annotation.EverythingIsNonnullByDefault;
import com.github.strongbad.hermitage.config.IsolationProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;

import static java.util.Objects.requireNonNull;

@Component
@EverythingIsNonnullByDefault
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class HermitageTransactionDefinition implements TransactionDefinition {

  private final Isolation isolation;

  private HermitageTransactionDefinition(IsolationProperties hermitageProperties) {
    this.isolation = requireNonNull(hermitageProperties.getIsolation());
  }

  @Override
  public int getPropagationBehavior() {
    return TransactionDefinition.PROPAGATION_REQUIRES_NEW;
  }

  @Override
  public int getIsolationLevel() {
    return isolation.value();
  }

  @Override
  public int getTimeout() {
    return TIMEOUT_DEFAULT;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public String getName() {
    return toString();
  }

}
