package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;

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
	public RetryTemplate retryTemplate() {
		return new RetryTemplate();
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
