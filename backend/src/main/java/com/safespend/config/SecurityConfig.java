package com.safespend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults()) // use the bean from CorsConfig
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
        .requestMatchers("/actuator/**").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/alerts/**", "/api/transactions/**").hasAnyRole("USER","ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/transactions/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .httpBasic(Customizer.withDefaults())
      .build();
  }

  @Bean
  public UserDetailsService users() {
    UserDetails user = User.withUsername("user").password("{noop}user123").roles("USER").build();
    UserDetails admin = User.withUsername("admin").password("{noop}admin123").roles("ADMIN").build();
    return new InMemoryUserDetailsManager(user, admin);
  }
}
