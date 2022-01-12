package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import com.github.egrmdev.bidder.model.BidRequest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BidderClient {
    private final WebClient client;
    private final String bidderUrl;

    public BidderClient(WebClient.Builder builder, String bidderUrl) {
        this.bidderUrl = bidderUrl;
        this.client = builder.baseUrl(bidderUrl).build();
    }

    public Mono<AuctionBid> getAuctionBid(BidRequest bidRequest) {
        return client.post().uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(bidRequest))
                .retrieve()
                .bodyToMono(AuctionBid.class);
    }

    public String getBidderUrl() {
        return bidderUrl;
    }

}
