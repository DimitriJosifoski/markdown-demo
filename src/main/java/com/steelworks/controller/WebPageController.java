package com.steelworks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC controller serving Thymeleaf UI pages.
 */
@Controller
public class WebPageController {

    @GetMapping({"/", "/dashboard"})
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/lots")
    public String lotsPage() {
        return "lots";
    }
}
