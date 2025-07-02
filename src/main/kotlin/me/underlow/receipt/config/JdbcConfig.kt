package me.underlow.receipt.config

import me.underlow.receipt.repository.*
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
@Import(DataSourceAutoConfiguration::class)
class JdbcConfig {

    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Bean
    fun userRepository(jdbcTemplate: JdbcTemplate): UserRepository {
        return UserRepositoryImpl(jdbcTemplate)
    }

    @Bean
    fun loginEventRepository(jdbcTemplate: JdbcTemplate): LoginEventRepository {
        return LoginEventRepositoryImpl(jdbcTemplate)
    }

    @Bean
    fun serviceProviderRepository(jdbcTemplate: JdbcTemplate): ServiceProviderRepository {
        return ServiceProviderRepositoryImpl(jdbcTemplate)
    }

    @Bean
    fun paymentMethodRepository(jdbcTemplate: JdbcTemplate): PaymentMethodRepository {
        return PaymentMethodRepositoryImpl(jdbcTemplate)
    }

    @Bean
    fun billRepository(jdbcTemplate: JdbcTemplate): BillRepository {
        return BillRepositoryImpl(jdbcTemplate)
    }

    @Bean
    fun receiptRepository(jdbcTemplate: JdbcTemplate): ReceiptRepository {
        return ReceiptRepositoryImpl(jdbcTemplate)
    }

    @Bean
    fun paymentRepository(jdbcTemplate: JdbcTemplate): PaymentRepository {
        return PaymentRepositoryImpl(jdbcTemplate)
    }
}
