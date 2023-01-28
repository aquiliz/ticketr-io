package com.aquiliz.ticketr.booking;

import com.aquiliz.ticketr.booking.dto.PricingRequest;
import org.springframework.cloud.openfeign.FeignClient;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pricing-service")
public interface PricingServiceClient {
    @PostMapping("/price")
    BigDecimal getPrice(@RequestBody PricingRequest pricingRequest) ;
}