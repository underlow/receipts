package me.underlow.receipt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "receipts")
data class ReceiptsProperties(
    var inboxPath: String = "/data/inbox",
    var attachmentsPath: String = "/data/attachments",
    var openaiApiKey: String? = null,
    var claudeApiKey: String? = null,
    var googleAiApiKey: String? = null,
    )
