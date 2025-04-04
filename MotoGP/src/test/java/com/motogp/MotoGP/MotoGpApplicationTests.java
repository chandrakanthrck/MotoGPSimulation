package com.motogp.MotoGP;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("❌ Skipping context load test in CI as DB is not configured")
class MotoGpApplicationTests {

	@Test
	void contextLoads() {
	}
}
