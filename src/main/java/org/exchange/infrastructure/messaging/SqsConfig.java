package org.exchange.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;


@Configuration
public class SqsConfig {

    @Value("${aws.region:eu-west-1}")
    private String region;

    @Value("${aws.endpoint:}")
    private String endpoint; // localstack endpoint override

    @Bean
    public SqsClient sqsClient() {
        SqsClientBuilder b = SqsClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                ))
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isBlank()) {
            b.endpointOverride(URI.create(endpoint));
        }
        return b.build();
    }
}