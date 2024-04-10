package com.itermit.core.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.itermit.core.config.jwt.JwtUtils;
import com.itermit.core.controller.AuthController;
import com.itermit.core.controller.advice.ApplicationControllerAdvice;
import com.itermit.core.model.ERole;
import com.itermit.core.model.dto.UserDto;
import com.itermit.core.model.dto.request.*;
import com.itermit.core.model.dto.response.LoginResponse;
import com.itermit.core.model.entity.RefreshToken;
import com.itermit.core.model.entity.Role;
import com.itermit.core.model.entity.User;
import com.itermit.core.service.UserService;
import com.itermit.core.service.implementation.RefreshTokenService;
import com.itermit.core.service.implementation.UserDetailsImpl;
import com.itermit.core.service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final Long USER_ID = 1L;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    Authentication authentication;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private AuthController subject;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockServletContext servletContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        servletContext = new MockServletContext();
    }

    @Test
    void authenticateUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@mail.com", "pass");

        String accessTokenString = "accessToken1234567890abcdef";
        String refreshTokenString = "refreshToken1234567890abcdef";

        RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenString).build();

        User user = User.builder()
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .id(1L)
                .roles(Set.of(Role.builder().name(ERole.ROLE_USER).build()))
                .build();

        UserDetailsImpl jwtUserDetails = UserDetailsImpl.build(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(jwtUserDetails, null);

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        LoginResponse loginResponse = new LoginResponse(
                accessTokenString,
                refreshTokenString,
                USER_ID,
                "user@mail.com",
                List.of("ROLE_USER"));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(eq((UserDetailsImpl) auth.getPrincipal()))).thenReturn(loginResponse.getAccess_token());
        when(refreshTokenService.createRefreshToken(eq(userDetails.getId()))).thenReturn(refreshToken);

        RequestBuilder requestBuilder = post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(loginResponse))
                .accept(MediaType.APPLICATION_JSON);


        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loginResponse.getId().toString()))
                .andExpect(jsonPath("$.access_token").value(loginResponse.getAccess_token()))
                .andExpect(jsonPath("$.refresh_token").value(loginResponse.getRefresh_token()))
                .andExpect(jsonPath("$.username").value(loginResponse.getUsername()));

        verifyNoMoreInteractions(authenticationManager);

        verify(refreshTokenService).deleteByUserId(userDetails.getId());
        verify(refreshTokenService).createRefreshToken(userDetails.getId());
        verifyNoMoreInteractions(refreshTokenService);

        verify(jwtUtils).generateJwtToken(userDetails);
        verifyNoMoreInteractions(jwtUtils);
    }

    @Test
    void registerUser() throws Exception {
        User user = User.builder()
                .id(1L)
                .roles(Set.of(Role.builder().name(ERole.ROLE_USER).build()))
                .build();

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("user")
                .password("password")
                .build();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .role(Set.of("ROLE_USER"))
                .build();

        when(userService.create(any(CreateUserRequest.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createUserRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(userDto.getId()));

        verify(userService).create(any(CreateUserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void refreshtoken() throws Exception {
        String refreshTokenString = "12345";
        TokenRefreshRequest request = new TokenRefreshRequest(refreshTokenString);
        User user = User.builder().id(USER_ID).username("User").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .build();

        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);

        RequestBuilder requestBuilder = post("/auth/refreshtoken")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refresh_token").value(refreshTokenString));

        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtUtils).generateTokenFromUser(user);
        verifyNoMoreInteractions(refreshTokenService, jwtUtils);
    }

    @Test
    void changePassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest(1L, "new pass");

        RequestBuilder requestBuilder = patch("/auth/password")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        verify(userService).updatePassword(any(UpdatePasswordRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateAvatar() throws Exception {
        MockMultipartFile firstFile = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "some xml".getBytes()
        );

        RequestBuilder requestBuilder = multipart("/auth/avatar")
                .file(firstFile)
                .param("id", "1");

        when(userService.updateAvatar(eq(1L), any())).thenReturn("filename.txt");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        verify(userService).updateAvatar(eq(1L), eq(null));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getInfo() throws Exception {
        User user = User.builder()
                .username("user@mail.com")
                .password("pass")
                .id(1L)
                .roles(Set.of(Role.builder().name(ERole.ROLE_USER).build()))
                .build();

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());

        UserDetailsImpl jwtUserDetails = UserDetailsImpl.build(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(jwtUserDetails, null);

        when(userService.findById(any(Long.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = get("/auth/me")
                .principal(auth);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.username").value(userDto.getUsername()));


        verify(userService).findById(eq(1L));
        verifyNoMoreInteractions(userService);

        verify(userMapper).toDto(eq(user));
        verifyNoMoreInteractions(userMapper);

        verifyNoMoreInteractions(authentication);
    }

    @Test
    void logoutRequest() {
        MockHttpServletRequest request = logout().buildRequest(this.servletContext);
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getParameter(token.getParameterName())).isEqualTo(token.getToken());
        assertThat(request.getRequestURI()).isEqualTo("/logout");
    }
}