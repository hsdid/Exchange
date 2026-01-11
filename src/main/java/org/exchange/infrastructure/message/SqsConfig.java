package org.exchange.infrastructure.message;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SqsConfig {

    @Value("${spring.cloud.aws.region.static:eu-west-1}")
    private String region;

    @Value("${spring.cloud.aws.endpoint:}")
    private String endpoint;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        var builder = SqsAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                ))
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }
}