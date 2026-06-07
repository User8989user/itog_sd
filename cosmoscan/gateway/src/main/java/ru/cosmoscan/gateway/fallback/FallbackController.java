package ru.cosmoscan.gateway.fallback;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/storing")
    public Mono<Map<String, String>> storingFallback() {
        return Mono.just(Map.of(
            "error", "File Storing Service is temporarily unavailable",
            "status", "503"
        ));
    }
}