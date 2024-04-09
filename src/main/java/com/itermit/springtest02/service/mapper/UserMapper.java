package com.itermit.springtest02.service.mapper;

import com.itermit.springtest02.model.dto.UserDto;
import com.itermit.springtest02.model.entity.User;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserDto toDto(User user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        userDto.setRoles(roles);

        return userDto;
    }

}
