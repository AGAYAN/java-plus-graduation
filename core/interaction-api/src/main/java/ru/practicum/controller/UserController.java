package ru.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.user.UserDto;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserController {

    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);
}
