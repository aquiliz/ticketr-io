package com.aquiliz.ticketr.pricing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
public class PricingService {

    public BigDecimal calculatePrice(PricingRequest pricingRequest) {
        // Simulating a price calculation. In reality, multiple other microservices will be called here and a complex
        // formula will be used to calculate the final price. In addition all factors like baggage, extras,
        // seat class, etc) will be taken into account. This is of course an over-simplified version.
        BigDecimal price = BigDecimal.valueOf(50 + (3000 - 50) * new Random().nextDouble());
        log.info("Calculated price={} for flights={} , number of passengers = {}, or"
                + "igin={} , destination={}", price, pricingRequest.getFlights(),pricingRequest.getPassengers().size(),  pricingRequest.getOriginAirport(),
            pricingRequest.getDestinationAirport());
        return price;
    }
}
