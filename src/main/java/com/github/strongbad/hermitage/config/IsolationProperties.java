package com.github.strongbad.hermitage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;

import static java.util.Objects.requireNonNull;

@Component
@ConfigurationProperties(prefix = "hermitage.isolation")
public class IsolationProperties {

  private String level;

  public Isolation getIsolation() {
    return Isolation.valueOf(getLevel());
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = requireNonNull(level);
  }

}
