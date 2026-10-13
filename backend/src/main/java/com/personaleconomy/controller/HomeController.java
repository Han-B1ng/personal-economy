package com.personaleconomy.controller;

import com.personaleconomy.common.response.ApiResponse;
import com.personaleconomy.vo.HomeSnapshot;
import com.personaleconomy.service.HomeQueryService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@Profile("(bootstrap | mysql) & !prod")
public class HomeController {
    private final HomeQueryService queryService;

    public HomeController(HomeQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ApiResponse<HomeSnapshot> getHome() {
        return ApiResponse.success(queryService.getSnapshot());
    }
}
