package com.luv2code.springboot.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

//    @Bean
//    public InMemoryUserDetailsManager userDetailsManager() {
//        UserDetails alex = User.builder().username("alex").password("{noop}test123").roles("USER", "EMPLOYEE").build();
//        UserDetails vasia = User.builder().username("vasia").password("{noop}vasia").roles("USER").build();
//        return new InMemoryUserDetailsManager(alex, vasia);  // Return both users
//    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/loginPage","/css/**", "/js/**", "/images/**", "/ws/**" ).permitAll()  // Allow static resources like CSS, JS
//                        .requestMatchers("/api/users").hasRole("USER")
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/loginPage")
//                        .loginProcessingUrl("/authenticateTheUser")
//                        .permitAll()
//                );
//        return http.build();
//    }
}
