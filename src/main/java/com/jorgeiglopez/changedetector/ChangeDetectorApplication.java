package com.jorgeiglopez.changedetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ChangeDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChangeDetectorApplication.class, args);
        System.out.println("Springboot app running...");
	}

    @GetMapping("/")
    public String hello() {
        return "Hello, World!";
    }

}
