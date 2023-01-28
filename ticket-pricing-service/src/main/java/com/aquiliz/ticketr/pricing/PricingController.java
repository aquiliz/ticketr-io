package com.aquiliz.ticketr.pricing;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/price")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping
    public BigDecimal getPrice(@RequestBody PricingRequest pricingRequest) {
        return pricingService.calculatePrice(pricingRequest);
    }
}
