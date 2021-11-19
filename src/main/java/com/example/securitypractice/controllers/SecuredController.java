package com.example.securitypractice.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/secured")
public class SecuredController {

    @GetMapping
    public String securedMethod(@AuthenticationPrincipal OAuth2User principal) {
        return "Welcome to secured web page, mr " + principal.getName();
    }
}
