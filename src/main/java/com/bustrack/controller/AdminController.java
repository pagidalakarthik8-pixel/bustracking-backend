package com.bustrack.controller;

import com.bustrack.dto.Dtos.AssignBusRequest;
import com.bustrack.dto.Dtos.UserResponse;
import com.bustrack.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Student transportation management (admin only, enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/admin/students")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> students() {
        return userService.listStudents();
    }

    @PutMapping("/{id}/bus")
    public UserResponse assignBus(@PathVariable Long id, @RequestBody AssignBusRequest req) {
        return userService.assignBus(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteStudent(id);
    }
}
