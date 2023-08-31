package com.termine.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.termine.api.controller.UserController;
import com.termine.api.model.User;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

    @Bean
    public CommandLineRunner initializeDefaultUser() {
        return args -> {
            List<String> roles = Arrays.asList("user", "programmer");
            UserController.users.add(new User(1L, "Default User", roles));
        };
    }
}
