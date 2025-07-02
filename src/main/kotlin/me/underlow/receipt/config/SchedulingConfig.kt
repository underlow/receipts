package me.underlow.receipt.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Configuration class to enable Spring's scheduled task support.
 * This enables the @Scheduled annotation used in FileWatcherService.
 */
@Configuration
@EnableScheduling
class SchedulingConfig