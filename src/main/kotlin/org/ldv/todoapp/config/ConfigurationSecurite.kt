package org.ldv.todoapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class   ConfigurationSecurite {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { acces ->
                acces.requestMatchers(
                    "/css/**",
                    "/fonts/**",
                    "/images/**",
                    "/vendor/**"
                ).permitAll()
                acces.anyRequest().authenticated()
            }
            .formLogin { login ->
                login.loginPage("/login")
                    .permitAll()
            }
            .logout { logout ->
                logout.logoutSuccessUrl("/login?logout")
                    .permitAll()
            }

        return http.build()
    }
}
