package me.underlow.receipt.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration for static resources and CORS.
 * Configures proper serving of static CSS, JS, and other resources.
 */
@Configuration
class WebConfiguration(
    @Value("\${receipts.attachments-path}") private val attachmentsPath: String
) : WebMvcConfigurer {

    /**
     * Configure static resource handlers with proper MIME types.
     *
     * @param registry ResourceHandlerRegistry to configure
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600)
            .resourceChain(true)
            
        // Configure avatar/attachment serving from file system
        registry
            .addResourceHandler("/attachments/**")
            .addResourceLocations("file:$attachmentsPath/")
            .setCachePeriod(3600)
            .resourceChain(true)
    }

    /**
     * Configure CORS mapping to allow cross-origin requests.
     *
     * @param registry CorsRegistry to configure
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}