package com.naughtycodes.lab.options.app.models;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@PropertySource("classpath:application.properties")
public class AppProperties {
	
	@Value("${GetNseOptionsByExpiryUrl}")
	private String GetNseOptionsByExpiryUrl;
	
	@Value("${GetNseOptionsByPriceUrl}")
	private String GetNseOptionsByPriceUrl;
	
	@Value("${GetRsiUrl}")
	private String GetRsiUrl;
	
	@Value("${GetStockPriceUrl}")
	private String GetStockPriceUrl;
	
	@Value("${GitRepoDir}")
	private String GitRepoDir;
	
	@Value("${GitKey}")
	private String GitKey;

	public String getGetNseOptionsByExpiryUrl() {
		return GetNseOptionsByExpiryUrl;
	}

	public void setGetNseOptionsByExpiryUrl(String getNseOptionsByExpiryUrl) {
		GetNseOptionsByExpiryUrl = getNseOptionsByExpiryUrl;
	}

	public String getGetNseOptionsByPriceUrl() {
		return GetNseOptionsByPriceUrl;
	}

	public void setGetNseOptionsByPriceUrl(String getNseOptionsByPriceUrl) {
		GetNseOptionsByPriceUrl = getNseOptionsByPriceUrl;
	}

	public String getGetRsiUrl() {
		return GetRsiUrl;
	}

	public void setGetRsiUrl(String getRsiUrl) {
		GetRsiUrl = getRsiUrl;
	}

	public String getGetStockPriceUrl() {
		return GetStockPriceUrl;
	}

	public void setGetStockPriceUrl(String getStockPriceUrl) {
		GetStockPriceUrl = getStockPriceUrl;
	}

	public String getGitRepoDir() {
		return GitRepoDir;
	}

	public void setGitRepoDir(String gitRepoDir) {
		GitRepoDir = gitRepoDir;
	}

	public String getGitKey() {
		return GitKey;
	}

	public void setGitKey(String gitKey) {
		GitKey = gitKey;
	}

}
