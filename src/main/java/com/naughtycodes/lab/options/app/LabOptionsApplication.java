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
	
	private static final Logger LOGGER=LoggerFactory.getLogger(LabOptionsApplication.class);
	@Autowired private JobLauncher jobLauncher;
	@Autowired private Job job;
	
	public static void main(String[] args) throws IOException {
		
		ApplicationContext ctx  = SpringApplication.run(LabOptionsApplication.class, args);
		LOGGER.info("Fetch NSE options data application has started..!");
		
		String[] beanNameList = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNameList);
		for(String beanName : beanNameList) {
			 LOGGER.info("Bean Definition => "+beanName);
		}
		
	}
	
	@Scheduled(cron = "*/10 * * * * *")
	public void perform() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("JobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(job, params);
		
	}
	
}
