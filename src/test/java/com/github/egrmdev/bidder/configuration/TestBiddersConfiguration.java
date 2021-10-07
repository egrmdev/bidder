package com.github.egrmdev.bidder.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@TestConfiguration
public class TestBiddersConfiguration {
    @Bean
    public BiddersConfiguration getTestConfiguration() {
        final BiddersConfiguration biddersConfiguration = new BiddersConfiguration();
        biddersConfiguration.setBidders(List.of("http://localhost:8081", "http://localhost:8082", "http://localhost:8083"));
        return biddersConfiguration;
    }
}

