package com.github.egrmdev.bidder.model;

import lombok.Value;

@Value
public class AuctionBid {
    long id;
    long bid;
    String content;
}

