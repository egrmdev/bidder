package com.github.egrmdev.bidder.model;

import lombok.Value;

import java.util.Map;

@Value
public class BidRequest {
    long id;
    Map<String, String> attributes;
}

