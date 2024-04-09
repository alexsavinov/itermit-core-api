package com.itermit.springtest02.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itermit.springtest02.config.jwt.AuthTokenFilter;
import com.itermit.springtest02.config.jwt.JwtUtils;
import com.itermit.springtest02.model.dto.response.ErrorResponse;
import com.itermit.springtest02.model.dto.response.LoginResponse;
import com.itermit.springtest02.model.entity.RefreshToken;
import com.itermit.springtest02.service.implementation.RefreshTokenService;
import com.itermit.springtest02.service.implementation.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper mapper;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    @Value("${app.corsList}")
    String[] corsList;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()
                        .requestMatchers(
                                "/oauth2/**",
                                "/login**",
                                "/auth/login",
                                "/auth/register",
                                "/auth/refreshtoken"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/images/*/**",
                                "/articles",
                                "/articles/*/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/users",
                                "/articles",
                                "/articles/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/users/**",
                                "/articles/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/articles"
                        ).hasAnyRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/users"
                        ).hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .httpBasic(Customizer.withDefaults())
                .formLogin(login -> login
                        .successHandler(this::successHandler)
                        .failureHandler(authenticationFailureHandler())
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(this::authenticationEntryPoint)
                )
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                        .addLogoutHandler(this::logout)
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess));

        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new RestAuthenticationFailureHandler();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(corsList));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    private void logout(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) {
    }

    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                         Authentication authentication) {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void successHandler(HttpServletRequest request,
                                HttpServletResponse response, Authentication authentication) throws IOException {

        UserDetailsImpl userDetails;
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            userDetails = (UserDetailsImpl) authentication.getPrincipal();
        } else {
            DefaultOidcUser principal = (DefaultOidcUser) authentication.getPrincipal();
            String email = principal.getAttributes().get("email").toString();
            userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
        }

        String token = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        refreshTokenService.deleteByUserId(userDetails.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        LoginResponse loginResponse = new LoginResponse(token, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), roles);

        response.getWriter().write(mapper.writeValueAsString(loginResponse));
    }

    private void authenticationEntryPoint(HttpServletRequest request, HttpServletResponse response,
                                          AuthenticationException authException) throws IOException {
        if (authException instanceof BadCredentialsException) {
            response.getWriter().write(mapper.writeValueAsString(ErrorResponse.of(authException.getMessage(), 10402)));
        } else if (authException instanceof UsernameNotFoundException) {
            response.getWriter().write(mapper.writeValueAsString(ErrorResponse.of(authException.getMessage(), 10403)));
        } else {
            response.getWriter().write(mapper.writeValueAsString(Collections.singletonMap("error", "Unauthenticated")));
        }
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/images/*/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**"
        );
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}