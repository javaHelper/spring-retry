package com.example;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.example.client.LakeProfile;
import com.example.client.LakeProfileClient;

@SpringBootApplication
public class BassTrackerServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(BassTrackerServiceApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(BassTrackerServiceApplication.class, args);
	}
	
	// means Back Off = 0 & Max Attempts = 3
	@Bean
	public RetryTemplate retryTemplate(@Value("${retry.maxAttempts}") int maxRetryAttempts,
			@Value("${retry.fixed.backOfPeriod}") int fixedBackOfPeriod,
			@Value("${retry.exponential.initialBackoffInterval}") int initialInterval,
			@Value("${retry.exponential.maxBackoffInterval}") int expMaxBackoffInterval,
			@Value("${retry.exponential.backoffMultiplier}") int expBackoffMultiplier) {
		
		Map<Class<? extends Throwable>, Boolean> retryableException = new HashMap<>();
		retryableException.put(RestClientException.class, true);
		
		// retry exception black-list - Retry Template will not retry on 500 error
		retryableException.put(HttpClientErrorException.class, false);  // HTTP 400
		retryableException.put(HttpServerErrorException.class, false); // HTTP 500
		
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetryAttempts);
		
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(fixedBackOfPeriod);
		
		ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
		exponentialBackOffPolicy.setInitialInterval(initialInterval);
		exponentialBackOffPolicy.setMaxInterval(expMaxBackoffInterval);
		exponentialBackOffPolicy.setMultiplier(expBackoffMultiplier);
		
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}
	
	@Bean
    public LakeProfileClient lakeProfileClient(RestTemplateBuilder builder,
                                               @Value("${lakeProfileServiceBaseUrl}") String lakeProfileServiceBaseUrl,
                                               @Value("${connectTimeout}") int clientConnectTimeout,
                                               @Value("${readTimeout}") int readTimeout,
                                               RetryTemplate retryTemplate) {
        return new LakeProfileClient(clientFactory(clientConnectTimeout, readTimeout), lakeProfileServiceBaseUrl, retryTemplate);
    }
	
	private HttpComponentsClientHttpRequestFactory clientFactory(int clientConnectTimeout, int readTimeout) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setReadTimeout(readTimeout);
		factory.setConnectTimeout(clientConnectTimeout);
		return factory;
	}

	@Bean
    public CommandLineRunner run(LakeProfileClient lakeProfileClient) throws Exception {
        return args -> {
            new Thread(() -> {
                lakeProfileClient.createLakeProfile(
                        new LakeProfile(1L, "Strawberry Reservoir", "Utah", 40.175397, -111.102157));
            }).start();

            Thread.sleep(3000);

            LakeProfile strawberryLake = lakeProfileClient.getLakeProfile(1L);
            log.info(strawberryLake.toString());
        };
    }
}
