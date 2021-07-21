package com.naughtycodes.lab.options.app.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.naughtycodes.lab.options.app.batch.TaskFetchOptionChain;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
     
	@Autowired private FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	@Autowired private AppUtils appUtils;
	
    @Autowired
    private JobBuilderFactory jobs;
 
    @Autowired
    private StepBuilderFactory steps;
     
    @Bean
    public Step stepOne(){
        return steps.get("TaskFetchOptionChainCurrentMonth")
                .tasklet(new TaskFetchOptionChain(fetchOptionsDataService, appUtils, "CURRENT_MONTH", true))
                .build();
    }
     
    @Bean
    public Step stepTwo(){
        return steps.get("TaskFetchOptionChainNextMonth")
                .tasklet(new TaskFetchOptionChain(fetchOptionsDataService, appUtils, "NEXT_MONTH", true))
                .build();
    }
    
    @Bean
    public Step stepThree(){
        return steps.get("TaskFetchOptionChainLastMonth")
                .tasklet(new TaskFetchOptionChain(fetchOptionsDataService, appUtils, "LAST_MONTH", true))
                .build();
    }  
     
    @Bean
    public Job JobFetchNseOptionChain(){
        return jobs.get("JobUpdateOptionChain")
                .incrementer(new RunIdIncrementer())
                .start(stepOne())
                .next(stepTwo())
                .next(stepThree())
                .build();
    }
}