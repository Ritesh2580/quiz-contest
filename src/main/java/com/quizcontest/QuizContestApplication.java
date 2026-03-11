package com.quizcontest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.quizcontest")
@EnableScheduling
public class QuizContestApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizContestApplication.class, args);
    }
}