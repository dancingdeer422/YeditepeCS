package com.nguner.yeditepecardshop.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.HashSet;
import java.util.Set;



@Configuration
public class SecurityConfig {

    private String[] publicEndpoints() {
        return new String[]{
                "/login",
                "/register",
                "/my-account",
                "/packs",
                "/boxes",
                "/cases",
                "/css/**",
                "/images/**",
                "/home",
                "/api/user/register",
                "/",
                "/index",
                "/error",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/v2/api-docs/**",
                "/api/user/authcheck",
                "/api/test/**",  // Test endpoints'lerini public yap
                "/search",       // Arama sayfasını public yap
                "/api/products/search", // API arama endpoint'ini public yap
                "/shipping",
                "/contact",
                "/privacy-policy",
                "/terms",
                "/cancel",
                "/commercial-transaction"
        };
    }

    private String[] anonymousEndpoints() {
        return new String[] {
                "test"
        };
    }

    private String[] adminEndpoints() {
        return new String[] {
                "test"
        };
    }

//    @Bean
//    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
//        return http
//                .authorizeHttpRequests(req -> req
//                        .requestMatchers(publicEndpoints()).permitAll()
//                        .requestMatchers(anonymousEndpoints()).permitAll()
//                        .requestMatchers(adminEndpoints()).hasRole("ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .csrf(AbstractHttpConfigurer::disable)
//                //.formLogin(withDefaults())
//                .build();
//    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(req -> req
                        .requestMatchers(publicEndpoints()).permitAll()
                        .requestMatchers(adminEndpoints()).hasRole("ADMIN")
                        .requestMatchers("/profile", "/profile/**").authenticated()
                        .requestMatchers("/order/**").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/my-account")            // our GET endpoint
                        .loginProcessingUrl("/process-login") // <form action>
                        .usernameParameter("email")      // matches <input name="">
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/my-account?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
