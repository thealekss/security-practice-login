package com.example.securitypractice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/secured")
public class SecuredController {

    @GetMapping
    public String securedMethod(Principal principal) {
        return "Welcome to secured web page, mr " + principal.getName();
    }
}
