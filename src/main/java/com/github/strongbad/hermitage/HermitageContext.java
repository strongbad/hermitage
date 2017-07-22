package com.github.strongbad.hermitage;

import com.github.strongbad.hermitage.config.ThreadPoolProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;

import java.util.concurrent.*;

@SpringBootApplication
public class HermitageContext {

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  ExecutorService threadPool(ThreadPoolProperties properties) {
    return Executors.newFixedThreadPool(properties.getSize());
  }

}
