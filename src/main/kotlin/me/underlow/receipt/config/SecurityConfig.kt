package me.underlow.receipt.config

import me.underlow.receipt.service.CustomOAuth2UserService
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
//@Profile("!e2e")
class SecurityConfig(private val customOAuth2UserService: CustomOAuth2UserService) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/login", "/static/**", "/error").permitAll()
                    .requestMatchers("/api/files/**").authenticated()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2Login ->
                oauth2Login
                    .loginPage("/login")
                    .userInfoEndpoint { userInfo ->
                        userInfo.oidcUserService(customOAuth2UserService)
                    }
                    .defaultSuccessUrl("/dashboard", true)
            }
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            }
            .csrf { csrf ->
                csrf
                    .ignoringRequestMatchers("/api/files/**")
            }
        return http.build()
    }
}
