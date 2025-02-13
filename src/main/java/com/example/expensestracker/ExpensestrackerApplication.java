package com.example.expensestracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpensestrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensestrackerApplication.class, args);
	}

}
