package me.underlow.receipt.config

import me.underlow.receipt.repository.LoginEventRepository
import me.underlow.receipt.repository.LoginEventRepositoryImpl
import me.underlow.receipt.repository.UserRepository
import me.underlow.receipt.repository.UserRepositoryImpl
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
}
