package com.lamgnoah.hustoj.configuration;

import com.lamgnoah.hustoj.security.JwtAuthenticationEntryPoint;
import com.lamgnoah.hustoj.security.JwtAuthorizationTokenFilter;
import com.lamgnoah.hustoj.service.JwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.CacheControlConfig;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtUserDetailsService jwtUserDetailsService;

  private final JwtAuthenticationEntryPoint unauthorizedHandler;

  private final JwtAuthorizationTokenFilter authenticationTokenFilter;

  @Value("${jwt.header}")
  private String tokenHeader;

  @Value("${jwt.route.authentication.path}")
  private String authenticationPath;


  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(jwtUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoderBean());

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return new ProviderManager(daoAuthenticationProvider());
  }


  @Bean
  public PasswordEncoder passwordEncoderBean() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth ->
            auth.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/register").permitAll()
                    .requestMatchers("/api/v1/verify").permitAll()
                    .requestMatchers("/api/v1/comment/page**").permitAll()
                .requestMatchers("/api/v1/judge_server_heartbeat").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/problems/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/announcements").permitAll()
                .anyRequest().authenticated()
        );
    httpSecurity.authenticationProvider(daoAuthenticationProvider());
    httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

    httpSecurity.headers(headersConfigurer -> headersConfigurer
        .frameOptions(FrameOptionsConfig::sameOrigin)
        .cacheControl(CacheControlConfig::disable));

    return httpSecurity.build();


  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers(HttpMethod.POST , authenticationPath)
        .requestMatchers(HttpMethod.GET, "/api/v1/problems/**")
        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**")
        .requestMatchers(HttpMethod.GET, "/api/v1/announcements/**")
        .requestMatchers(HttpMethod.GET,
            "/",
            "/*.html",
            "/favicon.ico",
            "/**.html",
            "/**.css",
            "/**.js");
  }

}


