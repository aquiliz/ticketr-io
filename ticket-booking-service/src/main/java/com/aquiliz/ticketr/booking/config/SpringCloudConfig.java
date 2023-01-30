package com.aquiliz.ticketr.booking.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {

  @Bean
  public CircuitBreaker circuitBreaker() {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .slidingWindowType(SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(10)
        .failureRateThreshold(70.0f)
        .slowCallRateThreshold(70.0f)
        .slowCallDurationThreshold(Duration.ofSeconds(4))
        .waitDurationInOpenState(Duration.ofSeconds(15))
        .build();

    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    return registry.circuitBreaker("pricingService");
  }
}
