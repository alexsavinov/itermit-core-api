package com.itermit.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itermit.core.controller.UserController;
import com.itermit.core.controller.advice.ApplicationControllerAdvice;
import com.itermit.core.exception.UserAlreadyExistsException;
import com.itermit.core.exception.UserNotFoundException;
import com.itermit.core.model.dto.UserDto;
import com.itermit.core.model.dto.request.CreateUserRequest;
import com.itermit.core.model.dto.request.UpdateUserRequest;
import com.itermit.core.model.entity.User;
import com.itermit.core.service.UserService;
import com.itermit.core.service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final Long USER_ID = 1L;
    @InjectMocks
    private UserController subject;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PagedResourcesAssembler<User> pagedResourcesAssembler;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserById() throws Exception {
        User expectedUser = new User();
        UserDto userDto = UserDto.builder().id(USER_ID).username("User1").build();

        when(userService.findById(any(Long.class))).thenReturn(expectedUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(
                        get("/users/{id}", USER_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.username").value(userDto.getUsername()));

        verify(userService).findById(USER_ID);
        verify(userMapper).toDto(expectedUser);
        verifyNoMoreInteractions(userService, modelMapper);
    }

    @Test
    void getUserById_whenUserNotFoundExceptionIsThrows_returns404() throws Exception {
        String errorMessage = "User not found";

        when(userService.findById(any(Long.class))).thenThrow(new UserNotFoundException(errorMessage));

        mockMvc.perform(
                        get("/users/{id}", USER_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verify(userService).findById(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUsers() throws Exception {
        User expectedUser = User.builder().id(USER_ID).username("User").build();

        List<User> expectedUsers = List.of(expectedUser);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<User> pageableExpectedUsers = new PageImpl<>(expectedUsers, pageable, expectedUsers.size());

        when(userService.findAll(any(Pageable.class), any())).thenReturn(pageableExpectedUsers);

        mockMvc.perform(
                        get("/users")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                                .param("search", "name=test")
                )
                .andExpect(status().isOk());

        verify(userService).findAll(pageable, "name=test");
        verifyNoMoreInteractions(userService);
    }

    @Test
    void addUser() throws Exception {
        User createdUser = new User();
        UserDto userDto = UserDto.builder().id(USER_ID).username("User1").build();

        when(userService.create(any(CreateUserRequest.class))).thenReturn(createdUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createdUser))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.username").value(userDto.getUsername()));

        verify(userMapper).toDto(createdUser);
        verifyNoMoreInteractions(userService, modelMapper);
    }

    @Test
    void addUser_whenUserAlreadyExistsExceptionIsThrows_returns409() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();

        String errorMessage = "User already exists";

        when(userService.create(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        RequestBuilder requestBuilder = post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createUserRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verifyNoInteractions(modelMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser() throws Exception {
        User updatedUser = new User();
        UserDto userDto = UserDto.builder().id(USER_ID).username("User1").build();

        when(userService.update(any(UpdateUserRequest.class))).thenReturn(updatedUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = patch("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(updatedUser))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.username").value(userDto.getUsername()));

        verify(userMapper).toDto(updatedUser);
        verifyNoMoreInteractions(userService, modelMapper);
    }

    @Test
    void deleteUserById() throws Exception {
        RequestBuilder requestBuilder = delete("/users/{id}", USER_ID);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        verify(userService).delete(USER_ID);
        verifyNoMoreInteractions(userService);
    }
}