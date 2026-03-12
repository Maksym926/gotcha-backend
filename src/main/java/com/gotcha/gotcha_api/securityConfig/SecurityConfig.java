package com.gotcha.gotcha_api.securityConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private final JWTFilter jwtFilter;

    @Autowired
    private final UserDetailsService userDetailsService;


    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final RateLimitingFilter rateLimitingFilter;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST,"/api/register", "/api/login", "/api/logout", "/api/webhook/stripe", "/api/forgot-password", "/api/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/verify-email").permitAll()

                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        .requestMatchers("/api/member/subscription/**").hasAnyAuthority("ADMIN", "MEMBER")
                        .requestMatchers("/api/member/**").access(memberSubscriptionAuthorizationManager())

                        .anyRequest()
                        .authenticated())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);




        return http.build();
    }

    @Bean
    public AuthorizationManager<RequestAuthorizationContext> memberSubscriptionAuthorizationManager() {
        return new MemberSubscriptionAuthorizationManager();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();

    }
    @Bean
    public AuthenticationProvider authProvider(){ // Working with DAO Data access object
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        return provider;
    }
}
