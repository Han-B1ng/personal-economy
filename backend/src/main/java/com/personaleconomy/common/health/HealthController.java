package com.personaleconomy.common.health;

import com.personaleconomy.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/v1/health")
    public ApiResponse<ServiceHealth> health() {
        return ApiResponse.success(new ServiceHealth("UP", "personal-economy-backend"));
    }

    public record ServiceHealth(String status, String service) {}
}
