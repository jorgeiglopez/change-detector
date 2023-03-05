package com.jorgeiglopez.changedetector;

import com.jorgeiglopez.changedetector.service.ChangeListenerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@RestController
@Slf4j
public class ChangeDetectorApplication {

    private final ChangeListenerService listenerService;

    public ChangeDetectorApplication(ChangeListenerService listenerService) {
        this.listenerService = listenerService;
        log.info("Starting the app...");
        this.listenerService.startService();
    }

    public static void main(String[] args) {
        SpringApplication.run(ChangeDetectorApplication.class, args);
    }

    @GetMapping("/logs")
    public ResponseEntity<String> getLogs() throws IOException {
        //Path logFile = Paths.get(System.getProperty("logging.file.name"));
        Path logFile = Path.of("target/application-logs.log");
        String logs = new String(Files.readAllBytes(logFile));
        log.info("Reading the logs [{}]: {}", logFile, logs);
        return ResponseEntity.ok(logs);
    }
}
