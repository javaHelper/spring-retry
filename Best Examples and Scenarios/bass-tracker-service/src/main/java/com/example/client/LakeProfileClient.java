package com.example.client;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

public class LakeProfileClient {
    private RestTemplate restTemplate;
    private String lakeProfileServiceBaseUrl;
    private RetryTemplate retryTemplate;
    private boolean isFirstRequest = true;

    public LakeProfileClient(HttpComponentsClientHttpRequestFactory clientFactory, String lakeProfileServiceBaseUrl, RetryTemplate retryTemplate) {
        this.restTemplate = new RestTemplate(clientFactory);
        this.lakeProfileServiceBaseUrl = lakeProfileServiceBaseUrl;
        this.retryTemplate = retryTemplate;
    }

    public LakeProfile getLakeProfile(Long id) {
        String url = lakeProfileServiceBaseUrl + "/lake-profile/" + id;

        return retryTemplate.execute(context -> {
        	long currentTime = System.currentTimeMillis();
        	
        	//System.out.println("----- Get Lake Profile Attempts -----"+(!isFirstRequest ? " backoff duration : "+(currentTime - lastMillis) + " milliseconds" : ""));
        	//lastMillis = currentTime;
        	isFirstRequest = false;
        	return restTemplate.getForObject(url, LakeProfile.class);
        });
    }

    public void createLakeProfile(LakeProfile lakeProfile) {
        String url = lakeProfileServiceBaseUrl + "/lake-profile";
        
        retryTemplate.execute(context -> {
        	System.out.println("---- Create Lake Profile Attempts ----");
        	return restTemplate.postForObject(url, lakeProfile, LakeProfile.class);        	
        });
    }
}
