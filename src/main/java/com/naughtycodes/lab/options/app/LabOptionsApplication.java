package com.naughtycodes.lab.options.app;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class LabOptionsApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(LabOptionsApplication.class, args);
	}
}
