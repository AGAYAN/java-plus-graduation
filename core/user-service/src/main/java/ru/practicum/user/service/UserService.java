package ru.practicum.user.service;

import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

  UserDto addUser(UserDto userDto);

  void deleteUser(Long userId);

  List<UserDto> getUsers(List<Long> ids, int from, int size);

  UserDto getUser(Long userId);

}
