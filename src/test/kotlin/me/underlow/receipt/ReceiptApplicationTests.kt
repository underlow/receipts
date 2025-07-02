package me.underlow.receipt

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import me.underlow.receipt.config.JdbcConfig

@SpringBootTest
@ContextConfiguration(classes = [JdbcConfig::class])
class ReceiptApplicationTests {

	@Test
	fun contextLoads() {
	}

}
