package com.github.egrmdev.bidder.util;

import com.github.egrmdev.bidder.model.AuctionBid;

public class BidContentParser {
    private BidContentParser() {}

    public static String parseContent(AuctionBid bidResponse) {
        return bidResponse.getContent().replace("$price$", Long.toString(bidResponse.getBid()));
    }
}
