package com.example.securitypractice.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HomeController {

    @GetMapping("/")
    public String homePage(@AuthenticationPrincipal  OAuth2User principal) {
        if (principal != null) {
            return "Welcome, " + principal.getName();
        }
        return "Welcome!";
    }
}
