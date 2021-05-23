package com.naughtycodes.lab.options.app;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LabOptionsApplication {

	public static void main(String[] args) throws IOException {
		//SpringApplication.run(LabOptionsApplication.class, args);
			try {
				FetchOptionsData fd = new FetchOptionsData();
				System.out.println(fd.getData());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		
	}
}
