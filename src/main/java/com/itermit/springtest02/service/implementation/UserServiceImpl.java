package com.itermit.springtest02.service.implementation;

import com.itermit.springtest02.exception.RoleNotFoundException;
import com.itermit.springtest02.exception.UserAlreadyExistsException;
import com.itermit.springtest02.exception.UserIncorrectException;
import com.itermit.springtest02.exception.UserNotFoundException;
import com.itermit.springtest02.model.ERole;
import com.itermit.springtest02.model.dto.request.CreateUserRequest;
import com.itermit.springtest02.model.dto.request.UpdatePasswordRequest;
import com.itermit.springtest02.model.dto.request.UpdateUserRequest;
import com.itermit.springtest02.model.entity.Profile;
import com.itermit.springtest02.model.entity.Role;
import com.itermit.springtest02.model.entity.User;
import com.itermit.springtest02.repository.PageableUserRepository;
import com.itermit.springtest02.repository.RoleRepository;
import com.itermit.springtest02.repository.UserRepository;
import com.itermit.springtest02.repository.specification.UserSpecs;
import com.itermit.springtest02.service.UserService;
import com.itermit.springtest02.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PageableUserRepository pageableUserRepository;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUtils fileUtils;

    @Override
    public User findById(Long id) {
        log.debug("Looking for a user with id {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Requested resource not found (id = %s)"
                        .formatted(id)
                ));

        log.info("Retrieved a user with id {}", id);
        return user;
    }

    @Override
    public Page<User> findAll(Pageable pageable, String search) {
        log.debug("Retrieving users. Page request: {}", pageable);

        Page<User> users;
        if (ofNullable(search).isEmpty() || search.equals("")) {
            users = pageableUserRepository.findAll(pageable);
        } else {
            users = pageableUserRepository.findAll(
                    Specification.where(UserSpecs.containsTextInName(search)),
                    pageable);
        }

        log.info("Retrieved {} users of {} total", users.getSize(), users.getTotalElements());
        return users;
    }

    @Override
    public User findByName(String name) {
        log.debug("Looking for a user with name {}", name);
        Optional<User> user = userRepository.findByUsername(name);

        user.orElseThrow(() -> new UserNotFoundException(
                "Requested resource not found (name = %s)".formatted(name)
        ));

        log.info("Found a user with name {}", name);
        return user.get();
    }

    @Override
    public User findByUsername(String username) {
        log.debug("Looking for a user with username {}", username);
        Optional<User> user = userRepository.findByUsername(username);

        user.orElseThrow(() -> new UserNotFoundException(
                "Requested resource not found (username = %s)".formatted(username)
        ));

        log.info("Found a user with username {}", username);
        return user.get();
    }

    @Override
    public User findByEmail(String email) {
        log.debug("Looking for a user with email {}", email);
        Optional<User> user = userRepository.findByUsername(email);

        user.orElseThrow(() -> new UserNotFoundException(
                "Requested resource not found (email = %s)".formatted(email)
        ));

        log.info("Found a user with email {}", email);
        return user.get();
    }


    @Override
    public User create(CreateUserRequest createRequest) {
        log.debug("Creating a new user");

        User newUser = User.builder()
                .username(createRequest.getUsername())
                .password(passwordEncoder.encode(createRequest.getPassword()))
                .roles(parseRoles(createRequest.getRole()))
                .profile(createRequest.getProfile())
                .build();

        try {
            User createdUser = userRepository.save(newUser);

            log.info("Created a new user with id {}", createdUser.getId());
            return createdUser;
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(
                    "Requested resource already exists (username = %s)".formatted(createRequest.getUsername()));
        }
    }

    @Override
    public User update(UpdateUserRequest updateRequest) {
        log.debug("Updating user");

        checkIdOfCurrentUser(updateRequest.getId());

        User user = findById(updateRequest.getId());

        ofNullable(updateRequest.getUsername()).ifPresent(user::setUsername);
        ofNullable(updateRequest.getRole()).ifPresent(roles ->
                user.setRoles(parseRoles(roles))
        );

        Profile profile = user.getProfile();
        ofNullable(updateRequest.getProfile()).ifPresent(profileRequest -> {
                    ofNullable(profileRequest.getName()).ifPresent(profile::setName);
                    ofNullable(profileRequest.getSurname()).ifPresent(profile::setSurname);
                    ofNullable(profileRequest.getGender()).ifPresent(profile::setGender);
                    ofNullable(profileRequest.getCity()).ifPresent(profile::setCity);
                    ofNullable(profileRequest.getAddress()).ifPresent(profile::setAddress);
                    ofNullable(profileRequest.getCompany()).ifPresent(profile::setCompany);
                    ofNullable(profileRequest.getMobile()).ifPresent(profile::setMobile);
                    ofNullable(profileRequest.getTele()).ifPresent(profile::setTele);
                    ofNullable(profileRequest.getWebsite()).ifPresent(profile::setWebsite);
                    ofNullable(profileRequest.getDate()).ifPresent(profile::setDate);
                    ofNullable(profileRequest.getAvatar()).ifPresent(profile::setAvatar);
                    profile.setId(user.getProfile().getId());
                    user.clearLastUpdateDate();
                    user.setProfile(profile);
                }
        );

        User updatedUser = userRepository.save(user);

        log.info("Updated a user with id {}", updatedUser.getId());
        return updatedUser;
    }

    @Override
    public void updatePassword(UpdatePasswordRequest updateRequest) {
        log.debug("Updating user's password");

        checkIdOfCurrentUser(updateRequest.getId());

        User user = findById(updateRequest.getId());
        user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));

        User updatedUser = userRepository.save(user);

        log.info("Password updated for user with id {}", updatedUser.getId());
    }

    @Override
    public String updateAvatar(Long id, MultipartFile avatar) {
        log.debug("Updating user's avatar");

        checkIdOfCurrentUser(id);

        String fileName = fileUtils.saveAvatar(avatar);

        User user = findById(id);
        Profile profile = user.getProfile();

        profile.setAvatar(fileName);
        user.setProfile(profile);

        User updatedUser = userRepository.save(user);

        log.info("Avatar updated for user with id {}, filename: {}", updatedUser.getId(), fileName);

        return fileName;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting user with id {}", id);

        User foundUser = findById(id);

        userRepository.delete(foundUser);
        refreshTokenService.deleteByUserId(id);

        log.info("User with id {} is deleted", foundUser.getId());
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByUsername(email);
    }

    public void checkIdOfCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean roleAdmin = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!roleAdmin && userDetails.getId() != id) {
            throw new UserIncorrectException(
                    "User id (%d) belongs to another user".formatted(id)
            );
        }
    }

    private Set<Role> parseRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        if (ofNullable(strRoles).isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RoleNotFoundException("Error: Role USER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if (role.equals("admin")) {
                    throw new RoleNotFoundException("Role ADMIN cannot be assigned on registration.");
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RoleNotFoundException("Role USER is not found."));
                    roles.add(userRole);
                }
            });
        }

        return roles;
    }
}
