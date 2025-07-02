package me.underlow.receipt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "receipts")
data class ReceiptsProperties(
    val inboxPath: String = "/data/inbox",
    val attachmentsPath: String = "/data/attachments",
    val openaiApiKey: String? = null,
    val claudeApiKey: String? = null,
    val googleAiApiKey: String? = null,
    val googleClientId: String? = null,
    val googleClientSecret: String? = null,
    val databaseType: String = "sqlite" // Default to SQLite for development
)
