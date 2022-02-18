package com.aquiliz.ticketr.booking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "pricing-service")
public interface PricingServiceClient {
    @GetMapping("/price/{userId}/{seat}/{originAirport}/{destinationAirport}")
    BigDecimal getPrice(@PathVariable String userId, @PathVariable String seat, @PathVariable String originAirport,
                        @PathVariable String destinationAirport);
}