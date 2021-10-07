package com.github.egrmdev.bidder.web;

import com.github.egrmdev.bidder.service.BiddingService;
import com.github.egrmdev.bidder.model.BidRequest;
import com.github.egrmdev.bidder.util.BidContentParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AuctionHandler {

    private BiddingService biddingService;

    @Autowired
    public AuctionHandler(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    public Mono<ServerResponse> getWinningBidContent(ServerRequest request) {
        long bidId = Long.parseLong(request.pathVariable("id"));
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(biddingService.getHighestBid(new BidRequest(bidId, request.queryParams().toSingleValueMap()))
                                .map(BidContentParser::parseContent),
                        String.class);
    }
}
