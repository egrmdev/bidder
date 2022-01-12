package com.github.egrmdev.bidder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.egrmdev.bidder.configuration.TestBiddersConfiguration;
import com.github.egrmdev.bidder.model.AuctionBid;
import com.github.egrmdev.bidder.model.BidRequest;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestBiddersConfiguration.class)
class BiddingServiceTest {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BiddingService biddingService;

    @RegisterExtension
    static WireMockExtension BIDDER_1 = getConfiguredInstance(8081);

    @RegisterExtension
    static WireMockExtension BIDDER_2 = getConfiguredInstance(8082);

    @RegisterExtension
    static WireMockExtension BIDDER_3 = getConfiguredInstance(8083);

    @Test
    @DisplayName("Auction should return bid response with the highest bid if no decider is set explicitly")
    void shouldUseHighestBidWinnerDeciderIfNoDeciderSet() {
        biddingService.setWinnerDecider(null);
        long bidId = 0L;
        BIDDER_1.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 10, "")));
        BIDDER_2.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 20, "")));
        BIDDER_3.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 21, "")));

        assertAuctionResult(bidId, 21);
    }

    @Test
    @DisplayName("Auction should return bid response with the highest bid")
    void shouldReturnCorrectAuctionResultWithHighestBidDecider() {
        biddingService.setWinnerDecider(BiddingService.HIGHEST_BID);
        long bidId = 0L;
        BIDDER_1.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 10, "")));
        BIDDER_2.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 20, "")));
        BIDDER_3.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 21, "")));

        assertAuctionResult(bidId, 21);
    }

    @Test
    @DisplayName("Auction should return bid response with the second highest bid plus one")
    void shouldReturnCorrectAuctionResultWithSecondHighestBidPlusOneDecider() {
        biddingService.setWinnerDecider(new SecondHighestBidPlusOne());
        long bidId = 0L;
        BIDDER_1.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 10, "")));
        BIDDER_2.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 20, "")));
        BIDDER_3.stubFor(getWireMockStubMapping(new AuctionBid(bidId, 30, "")));

        assertAuctionResult(bidId, 21);
    }

    @Test
    @DisplayName("Action should not wait for slow bidders to respond")
    void shouldNotWaitForSlowBidders() {
        biddingService.setWinnerDecider(BiddingService.HIGHEST_BID);
        final AuctionBid response1 = new AuctionBid(1L, 10, "");
        BIDDER_1.stubFor(getWireMockStubMapping(response1));
        final AuctionBid response2 = new AuctionBid(1L, 100, "");
        BIDDER_2.stubFor(getWireMockStubMapping(response2));
        final AuctionBid response3 = new AuctionBid(1L, 10_000, "");
        BIDDER_3.stubFor(getURLWithHeaderBuilder().willReturn(getResponseDefBuilder(response3)
                .withFixedDelay(600))
        );
        // auction will not wait for the third bidder even though it'd have the highest bid
        // because its delay is higher than auction waiting time for the response
        assertAuctionResult(response1.getId(), response2.getBid());
    }

    @Test
    @DisplayName("Auction should not fail in case bidders return errors")
    void shouldNotFailInCaseOneBidderReturnsError() {
        biddingService.setWinnerDecider(BiddingService.HIGHEST_BID);
        final AuctionBid response1 = new AuctionBid(1L, 11, "");
        BIDDER_1.stubFor(getWireMockStubMapping(response1));
        BIDDER_2.stubFor(getURLWithHeaderBuilder().willReturn(aResponse().withStatus(404)));
        BIDDER_3.stubFor(getURLWithHeaderBuilder().willReturn(aResponse().withStatus(422)));

        assertAuctionResult(response1.getId(), response1.getBid());
    }

    private static WireMockExtension getConfiguredInstance(int port) {
        return WireMockExtension.newInstance()
                .options(WireMockConfiguration.wireMockConfig().port(port))
                .failOnUnmatchedRequests(true)
                .build();
    }

    private static MappingBuilder getWireMockStubMapping(AuctionBid response) {
        return getURLWithHeaderBuilder()
                .willReturn(getResponseDefBuilder(response).withUniformRandomDelay(1, 10));
    }

    private static MappingBuilder getURLWithHeaderBuilder() {
        return post(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"));
    }

    private static ResponseDefinitionBuilder getResponseDefBuilder(AuctionBid response) {
        return aResponse()
                .withHeader("Content-Type", "application/json")
                .withJsonBody(OBJECT_MAPPER.valueToTree(response));
    }

    private void assertAuctionResult(long bidId, long winningBid) {
        StepVerifier.create(biddingService.getHighestBid(new BidRequest(bidId, Map.of())))
                .expectNextMatches(bidResponse -> bidResponse.getBid() == winningBid
                        && bidResponse.getId() == bidId)
                .verifyComplete();
    }
}