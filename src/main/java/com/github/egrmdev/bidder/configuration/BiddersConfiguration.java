package com.github.egrmdev.bidder.configuration;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@ConfigurationProperties
@Validated
@Profile("prod")
@Configuration
public class BiddersConfiguration {
    @NotEmpty
    private List<@URL(regexp = "^https?://.*$") String> bidders;

    public void setBidders(List<String> bidders) {
        this.bidders = bidders;
    }

    public List<String> getBidders() {
        return bidders;
    }
}
