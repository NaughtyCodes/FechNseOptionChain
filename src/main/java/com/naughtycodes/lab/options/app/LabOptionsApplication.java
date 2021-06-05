package com.naughtycodes.lab.options.app;

import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EnableScheduling
public class LabOptionsApplication {
	
	@Autowired private JobLauncher jobLauncher;
	@Autowired private Job job;
	
	private static final Logger LOGGER=LoggerFactory.getLogger(LabOptionsApplication.class);
	
	public static void main(String[] args) throws IOException {
		
		ApplicationContext ctx  = SpringApplication.run(LabOptionsApplication.class, args);
		LOGGER.info("Fetch NSE options data application has started..!");
		
		String[] beanNameList = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNameList);
		for(String beanName : beanNameList) {
			 LOGGER.info("Bean Definition => "+beanName);
		}
		
	}

	//Batch runs on every one hour between 8pm-to-12pm in weekdays monday to friday
	@Scheduled(cron = "0 0/60 20-23 * * MON-FRI")
	public void perform() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("BatchUpdateOptionChain", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(job, params);
		
	}
	
}
