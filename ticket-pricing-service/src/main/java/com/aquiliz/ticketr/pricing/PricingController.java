package com.aquiliz.ticketr.pricing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/price")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/{userId}/{seat}/{originAirport}/{destinationAirport}")
    public BigDecimal getPrice(@PathVariable String userId, @PathVariable String seat, @PathVariable String originAirport,
                               @PathVariable String destinationAirport) {
        return pricingService.calculatePrice(userId, seat, originAirport, destinationAirport);
    }
}
