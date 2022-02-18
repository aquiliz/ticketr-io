package com.aquiliz.ticketr.pricing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
public class PricingService {

    public BigDecimal calculatePrice(String userId, String seat, String originAirport, String destinationAirport) {
        // Simulating a price calculation. In reality, multiple other microservices will be called here and a complex
        // formula will be used to calculate the final price. In addition, multiple other factors (like baggage, extras,
        // priority boarding, etc) will be taken into account. This is of course an over-simplified version.
        BigDecimal price = BigDecimal.valueOf(50 + (3000 - 50) * new Random().nextDouble());
        log.info("Calculated price={} for userId={} , origin={} , destination={}", price, userId, originAirport,
                destinationAirport);
        return price;

    }
}
