package com.example.k8;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String get() {
        return "test";
    }

    @GetMapping("/healthz")
    public void healthz() {
    }

    @GetMapping("/readyz")
    public void readyz() {
    }
}
