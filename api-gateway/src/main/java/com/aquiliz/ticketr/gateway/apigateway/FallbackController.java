package com.aquiliz.ticketr.gateway.apigateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

  @RequestMapping("/fallback")
  public ResponseEntity<String> fallback() {
    return ResponseEntity.ok(
        "One of our services in unavailable at the moment. Please try again later."
            + " We apologise for the inconvenience.");
  }
}
