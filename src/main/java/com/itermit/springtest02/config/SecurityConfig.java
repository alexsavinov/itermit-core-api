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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final TokenStore tokenStore;
    private final TokenFilter tokenFilter;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    //    private final AuthTokenFilter authenticationJwtTokenFilter;

    //    @Resource
//    private UserDetailsService userDetailsService;
    private final UserDetailsService userDetailsService;

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
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .cors(cors -> cors.disable())
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
//                .formLogin(Customizer.withDefaults())
                .formLogin(login -> login
                        .successHandler(this::successHandler)
                        .failureHandler(authenticationFailureHandler())
                )
//                .oauth2Login(Customizer.withDefaults())

//                .oauth2Login(oauth2 -> oauth2
//                        .authorizationEndpoint(authorization -> authorization
//                                .authorizationRequestRepository(new InMemoryRequestRepository())
//                        )
//                        .successHandler(this::successHandler)
//                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
////                        .authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/login/oauth2"))
                                .authenticationEntryPoint(this::authenticationEntryPoint)
                )

//                .and()
//                .authorizationRequestRepository(new InMemoryRequestRepository())
//                .and()
//                .authorizationEndpoint()
//                .successHandler(this::successHandler)
//                .and()
//                .exceptionHandling()
//                .authenticationEntryPoint(this::authenticationEntryPoint)

                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                        .addLogoutHandler(this::logout)
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess))

//                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
        ;

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
//        http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        System.out.println("!!!!!!!!!!!!! AuthenticationFailureHandler");
        return new RestAuthenticationFailureHandler();
    }

//    @Bean
//    public WebSecurityCustomizer ignoringCustomizer() {
//        return (web) -> web.ignoring().requestMatchers(
//                "/images/**",
//                "/images/avatars/**",
//                "/images/articles/**"
//        );
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost",
                "http://localhost:8183",
                "http://192.168.1.12",
                "http://192.168.1.12:8183",
                "http://localhost:8180"
        ));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "REDIRECT"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
//        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    private void logout(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) {
        // You can process token here
        System.out.println("!!!!!!!! logout !!!!!!!!!!!!!!!!!");
//        System.out.println("Auth token is - " + request.getHeader("Authorization"));
    }

    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                         Authentication authentication) throws IOException, ServletException {
        // this code is just sending the 200 ok response and preventing redirect
        System.out.println("!!!!!!!! onLogoutSuccess !!!!!!!!!!!!!!!!!");
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
//            String name = principal.getAttributes().get("name").toString();
//        "email" -> "spell477@gmail.com"
//        "family_name" -> "Savinov"
//        "name" -> "Oleksii Savinov"
//        "given_name" -> "Oleksii"
//        "locale" -> "ru"
//        "picture" -> "https://lh3.googleusercontent.com/a/ACg8ocKM42lHUiviYsfYdNJwxEPIJmLxJ2gbgSmUHPEYSh09=s96-c"

            userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
//            token = jwtUtils.generateJwtToken((UserDetailsImpl) authentication.getPrincipal());
        }

        String token = jwtUtils.generateJwtToken(userDetails);

//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//                loginRequest.getUsername(),
//                loginRequest.getPassword()
//        );
//
//        System.out.println("authenticateUser getUsername " + loginRequest.getUsername());
//        System.out.println("authenticateUser getPassword " + passwordEncoder.encode(loginRequest.getPassword()));
//
//        Authentication authentication = authenticationManager.authenticate(authenticationToken);
//        SecurityContextHolder.getContext().setAuthentication(authentication);/**/
//
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        String jwtToken = jwtUtils.generateJwtToken(userDetails);


//        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


//        String token = tokenStore.generateToken(authentication);


        System.out.println("!!!!!!!! successHandler ---> accessToken = " + token);
//        System.out.println("!!!!!!!! successHandler ---> jwtToken = " + jwtToken);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        refreshTokenService.deleteByUserId(userDetails.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        LoginResponse loginResponse = new LoginResponse(token, refreshToken.getToken(), userDetails.getId(),
//        JwtResponse jwtResponse = new JwtResponse(token, userDetails.getId(),
//                userDetails.getUsername(), userDetails.getEmail(), roles);
                userDetails.getUsername(), roles);

        response.getWriter().write(mapper.writeValueAsString(loginResponse));
//        response.getWriter().write(
//                mapper.writeValueAsString(Collections.singletonMap("accessToken", token))
//        );
    }

//    @Bean

    private void authenticationEntryPoint(HttpServletRequest request, HttpServletResponse response,
                                          AuthenticationException authException) throws IOException {
        System.out.println("--authenticationEntryPoint-- authException --" + authException);
        System.out.println("--authenticationEntryPoint-- " + authException.getMessage());
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


//http://localhost:8080/oauth2/authorization/google

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(userDetails);
//    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/images/*/**"
                , "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
        );
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}