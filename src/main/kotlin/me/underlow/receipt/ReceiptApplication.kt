package me.underlow.receipt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import me.underlow.receipt.config.JdbcConfig

@SpringBootApplication
@Import(JdbcConfig::class)
class ReceiptApplication

fun main(args: Array<String>) {
	runApplication<ReceiptApplication>(*args)
}
