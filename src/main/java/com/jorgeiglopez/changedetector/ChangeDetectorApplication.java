package com.jorgeiglopez.changedetector;

import com.jorgeiglopez.changedetector.service.ChangeListenerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Slf4j
public class ChangeDetectorApplication {

    private final ChangeListenerService listenerService;

    public ChangeDetectorApplication(ChangeListenerService listenerService) {
        this.listenerService = listenerService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChangeDetectorApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        log.info("Starting the app...");
        this.listenerService.startService();
        return "Hello, World!";
    }
}
