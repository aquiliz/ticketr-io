package com.aquiliz.ticketr.gateway.apigateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {

  @Bean
  public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route(r -> r.path("/booking/**")
            .filters(f -> f.circuitBreaker(
                config -> config.setName("booking").setFallbackUri("/fallback")))
            .uri("lb://ticket-booking-service"))
        .route(r -> r.path("/price/**")
            .filters(f -> f.circuitBreaker(
                config -> config.setName("booking").setFallbackUri("/fallback")))
            .uri("lb://pricing-service"))
        .build();
  }

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    //the circuit breaker trips (opens) if 70% of the last 10 calls fail or take more than 4 seconds to
    //complete (A failure in this case means thrown exception and not response code 500).
    //After it trips, it waits for 15 sec before switching back to half-open state.
    //Consistent successful responses when in half-open state causes it to switch to closed state again
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .slidingWindowType(SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(10)
        .failureRateThreshold(70.0f)
        .slowCallRateThreshold(70.0f)
        .slowCallDurationThreshold(Duration.ofSeconds(4))
        .waitDurationInOpenState(Duration.ofSeconds(15))
        .build();

    //If a request takes longer than the specified timeout, it will be considered as failed
    TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(4))
        .build();

    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .timeLimiterConfig(timeLimiterConfig)
        .circuitBreakerConfig(circuitBreakerConfig)
        .build());
  }
}
