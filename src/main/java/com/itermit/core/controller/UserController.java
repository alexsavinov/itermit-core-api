package com.itermit.core.controller;

import com.itermit.core.model.dto.UserDto;
import com.itermit.core.model.dto.UsersDto;
import com.itermit.core.model.dto.request.CreateUserRequest;
import com.itermit.core.model.dto.request.UpdateUserRequest;
import com.itermit.core.model.entity.User;
import com.itermit.core.service.UserService;
import com.itermit.core.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UsersDto usersDto;
    private final PagedResourcesAssembler<User> pagedResourcesAssembler;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User foundUser = userService.findById(id);
        UserDto userDto = userMapper.toDto(foundUser);

        userDto.add(linkTo(methodOn(UserController.class).getUserById(userDto.getId())).withSelfRel());
        userDto.add(linkTo(methodOn(UserController.class).getUsers(null, null)).withRel("collection"));
        return userDto;
    }

    @GetMapping
    public PagedModel<UserDto> getUsers(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<User> foundUsers = userService.findAll(pageable, search);

        return pagedResourcesAssembler.toModel(foundUsers, usersDto);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody CreateUserRequest createRequest) {
        User createdUser = userService.create(createRequest);
        UserDto userDto = userMapper.toDto(createdUser);

        userDto.add(linkTo(methodOn(UserController.class).addUser(createRequest)).withSelfRel());
        userDto.add(linkTo(methodOn(UserController.class).getUsers(null, null)).withRel("collection"));
        return userDto;
    }

    @PatchMapping
    public UserDto updateUser(@RequestBody UpdateUserRequest updateRequest) {
        User updatedUser = userService.update(updateRequest);
        UserDto userDto = userMapper.toDto(updatedUser);

        userDto.add(linkTo(methodOn(UserController.class).updateUser(updateRequest)).withSelfRel());
        userDto.add(linkTo(methodOn(UserController.class).getUsers(null, null)).withRel("collection"));
        return userDto;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long id) {
        userService.delete(id);
    }
}
