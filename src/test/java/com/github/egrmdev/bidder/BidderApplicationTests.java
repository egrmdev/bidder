package com.github.egrmdev.bidder;

import com.github.egrmdev.bidder.configuration.TestBiddersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestBiddersConfiguration.class)
class BidderApplicationTests {

	@Test
	void contextLoads() {
		// sanity check that context can be loaded
	}
}
