package com.itermit.springtest02.controller;


import com.itermit.springtest02.config.jwt.JwtUtils;
import com.itermit.springtest02.exception.RefreshTokenNotFoundException;
import com.itermit.springtest02.model.dto.UserDto;
import com.itermit.springtest02.model.dto.request.*;
import com.itermit.springtest02.model.dto.response.LoginResponse;
import com.itermit.springtest02.model.dto.response.TokenRefreshResponse;
import com.itermit.springtest02.model.entity.Profile;
import com.itermit.springtest02.model.entity.RefreshToken;
import com.itermit.springtest02.model.entity.User;
import com.itermit.springtest02.service.UserService;
import com.itermit.springtest02.service.implementation.RefreshTokenService;
import com.itermit.springtest02.service.implementation.UserDetailsImpl;
import com.itermit.springtest02.service.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public LoginResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        refreshTokenService.deleteByUserId(userDetails.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new LoginResponse(jwtToken, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), roles);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserDto registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        CreateUserRequest request = CreateUserRequest.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .role(Collections.singleton("ROLE_USER"))
                .profile(new Profile())
                .build();
        User user = userService.create(request);
        UserDto userDto = userMapper.toDto(user);

        userDto.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
        return userDto;
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changePassword(@RequestBody UpdatePasswordRequest updateRequest) {
        userService.updatePassword(updateRequest);
    }

    @PostMapping("/avatar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> updateAvatar(Long id, MultipartFile avatar) {
        String fileName = userService.updateAvatar(id, avatar);
        return Collections.singletonMap("avatar", fileName);
    }

    @GetMapping("/me")
    public UserDto getInfo(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userService.findById(userDetails.getId());
        UserDto userDto = userMapper.toDto(user);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        userDto.setRoles(roles);
        userDto.add(linkTo(methodOn(AuthController.class).getInfo(authentication)).withSelfRel());
        return userDto;
    }

    @PostMapping("/refreshtoken")
    public TokenRefreshResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUser(user);
                    return new TokenRefreshResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new RefreshTokenNotFoundException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
}