package com.github.egrmdev.bidder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BidderApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidderApplication.class, args);
        log.info("rock'n'roll");
    }

}

