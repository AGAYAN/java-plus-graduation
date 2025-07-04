package ru.practicum.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.user.mappers.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public UserDto addUser(UserDto userDto) {
    User user = UserMapper.mapToUser(userDto);
    return UserMapper.mapToUserDto(save(user));
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new EntityNotFoundException("User not found with id: " + userId);
    }
    userRepository.deleteById(userId);
  }

  @Override
  @Transactional
  public List<UserDto> getUsers(List<Long> ids, int from, int size) {
    Pageable pageable = PageRequest.of(from / size, size);

    Page<User> users;
    if (ids != null && !ids.isEmpty()) {
      users = userRepository.findAllByIdIn(ids, pageable);
    } else {
      users = userRepository.findAll(pageable);
    }

    return users.getContent().stream()
        .map(UserMapper::mapToUserDto)
        .collect(Collectors.toList());
  }

  @Override
  public UserDto getUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() ->
            new EntityNotFoundException("User not found with id: " + userId));
    return UserMapper.mapToUserDto(user);
  }

  private User save(User user) {
    log.debug("Saving user with email: {}", user.getEmail());

    try {
      return userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      log.warn("Failed to save user. Email already exists: {}", user.getEmail());
      throw new AlreadyExistsException("Email already exists.");
    }
  }
}
