package com.itermit.springtest02.service.implementation;

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
import com.itermit.springtest02.repository.RefreshTokenRepository;
import com.itermit.springtest02.repository.RoleRepository;
import com.itermit.springtest02.repository.UserRepository;
import com.itermit.springtest02.repository.specification.UserSpecs;
import com.itermit.springtest02.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;
    //    private static final Long CERTIFICATE_ID = 1L;
    private static final Long ORDER_ID = 1L;
    @InjectMocks
    private UserServiceImpl subject;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    //    @Mock
//    private OrderRepository orderRepository;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;
    @Mock
    UserDetailsImpl principal;
    @Mock
    private PageableUserRepository pageableUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private FileUtils fileUtils;

    @Test
    void findById() {
        User expectedUser = User.builder().id(USER_ID).build();

//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(principal);
//        when(principal.getUsername()).thenReturn("user1");
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(expectedUser));
        when(userRepository.findById(any(Long.class))).thenReturn(of(expectedUser));

//        SecurityContextHolder.setContext(securityContext);

        User actualUser = subject.findById(USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findById_whenUserIsNotFoundById_throwsUserNotFoundException() {
//        String username = "user1";

//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(principal);
//        when(principal.getUsername()).thenReturn(username);
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        SecurityContextHolder.setContext(securityContext);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findById(USER_ID));

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (id = " + USER_ID + ")";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findAll() {
        List<User> expectedUsers = List.of(new User());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<User> pageableExpectedUsers = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

//        when(pageableUserRepository.findAll(any(Pageable.class), any(String.class))).thenReturn(pageableExpectedUsers);
        when(pageableUserRepository.findAll(any(Pageable.class))).thenReturn(pageableExpectedUsers);

//        Page<User> actualUsers = subject.findAll(pageable, "name=test");
        Page<User> actualUsers = subject.findAll(pageable, "");

        verify(pageableUserRepository).findAll(pageable);
        verifyNoMoreInteractions(pageableUserRepository);

        assertThat(actualUsers).isEqualTo(pageableExpectedUsers);
    }

    @Test
    void findAll_withSearch() {
        List<User> expectedUsers = List.of(new User());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<User> pageableExpectedUsers = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        Specification<User> test = Specification.where(UserSpecs.containsTextInName("test"));

//        when(pageableUserRepository.findAll(any(Pageable.class), any(String.class))).thenReturn(pageableExpectedUsers);
//        when(pageableUserRepository.findAll(eq(test), any(Pageable.class))).thenReturn(pageableExpectedUsers);
        when(pageableUserRepository.findAll(any(), any(Pageable.class))).thenReturn(pageableExpectedUsers);

        Page<User> actualUsers = subject.findAll(pageable, "name=test");

//        verify(pageableUserRepository).findAll(eq(test), pageable);
//        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(pageableUserRepository);

        assertThat(actualUsers).isEqualTo(pageableExpectedUsers);
    }

    @Test
    void findByName() {
        User expectedUser = new User();

        when(userRepository.findByUsername(any(String.class))).thenReturn(of(expectedUser));

        User actualUser = subject.findByUsername("user1");

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findByName_whenUserWithNameNotExists_throwsUserNotFoundException() {
        User searchUser = User.builder().id(USER_ID).username("myUser").build();

        when(userRepository.findByUsername(any(String.class))).thenReturn(empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findByName(searchUser.getUsername()));

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (name = %s)".formatted(searchUser.getUsername());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findByUsername() {
        User expectedUser = new User();

        when(userRepository.findByUsername(any(String.class))).thenReturn(of(expectedUser));

        User actualUser = subject.findByUsername("user1");

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findByUsername_whenUserWithNameNotExists_throwsUserNotFoundException() {
        User searchUser = User.builder().id(USER_ID).username("myUser").build();

        when(userRepository.findByUsername(any(String.class))).thenReturn(empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findByUsername(searchUser.getUsername()));

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (username = %s)".formatted(searchUser.getUsername());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findByEmail() {
        User expectedUser = new User();

        when(userRepository.findByUsername(any(String.class))).thenReturn(of(expectedUser));

        User actualUser = subject.findByEmail("user1@mail.com");

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findByEmail_whenUserWithNameNotExists_throwsUserNotFoundException() {
        User searchUser = User.builder().id(USER_ID).username("myUser").build();

        when(userRepository.findByUsername(any(String.class))).thenReturn(empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findByEmail(searchUser.getUsername()));

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (email = %s)".formatted(searchUser.getUsername());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void create() {
        CreateUserRequest createRequest = CreateUserRequest.builder().username("myUser").password("pass").build();

        User expectedUser = User.builder().id(USER_ID).username("myUser").build();

        Role role = Role.builder().name(ERole.ROLE_USER).build();

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(roleRepository.findByName(any(ERole.class))).thenReturn(of(role));

        User actualUser = subject.create(createRequest);

        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void update() {
        Role role = Role.builder().name(ERole.ROLE_ADMIN).build();
        Profile profile = Profile.builder().id(1L).name("user").build();
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .id(USER_ID)
                .username("myUser")
//                .password("pass")
                .role(Set.of("ROLE_ADMIN"))
                .profile(profile)
                .build();


        User expectedUser = User.builder()
                .id(USER_ID)
                .username("myUser")
                .password("pass")
                .profile(profile)
                .roles(Set.of(Role.builder().name(ERole.ROLE_ADMIN).build()))
                .build();

        UserDetailsImpl userDetails = UserDetailsImpl.build(expectedUser);

        Authentication authentication1 = new TestingAuthenticationToken(
                userDetails, // principal
                null, // credentials
                "ROLE_ADMIN"); // authority roles

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(roleRepository.findByName(any(ERole.class))).thenReturn(of(role));
        when(securityContext.getAuthentication()).thenReturn(authentication1);
        when(userRepository.findById(any(Long.class))).thenReturn(of(expectedUser));
//        when(authentication.getPrincipal()).thenReturn(principal);
//        when(authentication.getAuthorities()).thenReturn(authorities);
//        when(principal.getUsername()).thenReturn("user1");
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(expectedUser));

        SecurityContextHolder.setContext(securityContext);

        User actualUser = subject.update(updateRequest);

        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);

        assertThat(userDetails.getPassword()).isEqualTo("pass");
        assertThat(userDetails.getAuthorities()).isEqualTo(

                new ArrayList<>(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        assertThat(userDetails.getUsername()).isEqualTo("myUser");
        assertThat(userDetails.isAccountNonExpired()).isEqualTo(true);
        assertThat(userDetails.isAccountNonLocked()).isEqualTo(true);
        assertThat(userDetails.isCredentialsNonExpired()).isEqualTo(true);
        assertThat(userDetails.isEnabled()).isEqualTo(true);
        assertThat(userDetails.equals(userDetails)).isEqualTo(true);
        assertThat(userDetails.equals(null)).isEqualTo(false);
        assertThat(userDetails.equals(new UserDetailsImpl())).isEqualTo(false);
    }

    @Test
    void update_whenUserIsNotAdmin_throwsUserIncorrectException() {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .id(2L)
                .username("myUser")
                .role(Set.of("ROLE_USER"))
                .build();

        User expectedUser = User.builder()
                .id(USER_ID)
                .username("myUser")
                .password("pass")
                .roles(Set.of(Role.builder().name(ERole.ROLE_USER).build()))
                .build();

        UserDetailsImpl userDetails = UserDetailsImpl.build(expectedUser);

        Authentication authentication1 = new TestingAuthenticationToken(
                userDetails, null, "ROLE_USER"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication1);

        SecurityContextHolder.setContext(securityContext);

        UserIncorrectException exception = assertThrows(UserIncorrectException.class,
                () -> subject.update(updateRequest));

        String expectedMessage = "User id (2) belongs to another user";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void delete() {
        User deleteUser = User.builder().id(USER_ID).username("myUser").build();

//        List<Order> expectedOrders = new ArrayList<>();

//        Pageable pageable = PageRequest.of(0, 5);
//        Page<Order> pageableExpectedOrders = new PageImpl(expectedOrders, pageable, expectedOrders.size());

//        when(orderRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(pageableExpectedOrders);


        when(userRepository.findById(any(Long.class))).thenReturn(of(deleteUser));
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(principal);
//        when(principal.getUsername()).thenReturn("user1");
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(deleteUser));

        SecurityContextHolder.setContext(securityContext);

        subject.delete(USER_ID);

//        verify(refreshTokenRepository).deleteByUserId(USER_ID);
//        verifyNoMoreInteractions(refreshTokenRepository);

        verify(refreshTokenService).deleteByUserId(any(Long.class));
        verifyNoMoreInteractions(refreshTokenService);

//        verify(userRepository).delete(deleteUser);
//        verifyNoMoreInteractions(userRepository);
    }

//    @Test
//    void delete_whenUserOrdersFound_thenThrowsUserCannotDeleteException() {
//        User deleteUser = User.builder().id(USER_ID).name("myUser").build();
//
//        GiftCertificate certificate = GiftCertificate.builder()
//                .id(CERTIFICATE_ID)
//                .name("cert")
//                .build();
//
//        Order expectedOrder = Order.builder()
//                .id(ORDER_ID)
//                .price(10.2)
//                .user(deleteUser)
//                .giftCertificate(certificate)
//                .build();
//
//        List<Order> expectedOrders = List.of(expectedOrder);
//
//        Pageable pageable = PageRequest.of(0, 5);
//        Page<Order> pageableExpectedOrders = new PageImpl(expectedOrders, pageable, expectedOrders.size());
//
//        when(orderRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(pageableExpectedOrders);
//
//        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(deleteUser));
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(principal);
//        when(principal.getUsername()).thenReturn("user1");
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(deleteUser));
//
//        SecurityContextHolder.setContext(securityContext);
//
//        UserCannotDeleteException exception = assertThrows(UserCannotDeleteException.class,
//                () -> subject.delete(USER_ID));
//
//        String expectedMessage = "User cannot be deleted - has found in (%d) orders"
//                .formatted(pageableExpectedOrders.getTotalElements());
//        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
//    }

    @Test
    void existsByUsername() {
        String username = "user";

        when(userRepository.existsByUsername(any(String.class))).thenReturn(true);

        Boolean expectedResult = subject.existsByUsername(username);

        verify(userRepository).existsByUsername(any(String.class));
        assertThat(expectedResult).isEqualTo(true);
    }

//    @Test
//    void existsByEmail() {
//        String email = "user@mail.com";
//
//        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);
//
//        Boolean expectedResult = subject.existsByEmail(email);
//
//        verify(userRepository).existsByEmail(any(String.class));
//        assertThat(expectedResult).isEqualTo(true);
//    }


    @Test
    void updatePassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest(USER_ID, "new pass");

        User user = User.builder()
                .id(USER_ID)
                .username("myUser")
                .password("pass")
                .roles(Set.of(Role.builder().name(ERole.ROLE_ADMIN).build()))
                .build();

        Authentication authentication1 = new TestingAuthenticationToken(
                UserDetailsImpl.build(user), null, "ROLE_ADMIN"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication1);
        when(userRepository.findById(any(Long.class))).thenReturn(of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        SecurityContextHolder.setContext(securityContext);

        subject.updatePassword(request);

        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateAvatar() {
        Profile profile = Profile.builder().id(1L).name("user").build();
        User user = User.builder()
                .id(USER_ID)
                .username("myUser")
                .password("pass")
                .profile(profile)
                .roles(Set.of(Role.builder().name(ERole.ROLE_ADMIN).build()))
                .build();

        Authentication authentication1 = new TestingAuthenticationToken(
                UserDetailsImpl.build(user), null, "ROLE_ADMIN"
        );

        MockMultipartFile file = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "some xml".getBytes()
        );

        String fileName = "filename.txt";

        when(securityContext.getAuthentication()).thenReturn(authentication1);
        when(userRepository.findById(any(Long.class))).thenReturn(of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(fileUtils.saveAvatar(any(MultipartFile.class))).thenReturn(fileName);

        SecurityContextHolder.setContext(securityContext);

        String actualFileName = subject.updateAvatar(USER_ID, file);

        assertThat(actualFileName).isEqualTo(fileName);

        verify(userRepository).findById(any(Long.class));
        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(userRepository, securityContext);
    }

}