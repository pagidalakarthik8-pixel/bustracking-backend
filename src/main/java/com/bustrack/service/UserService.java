package com.bustrack.service;

import com.bustrack.dto.Dtos.AssignBusRequest;
import com.bustrack.dto.Dtos.UserResponse;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Role;
import com.bustrack.model.User;
import com.bustrack.repository.BusRepository;
import com.bustrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Admin operations on student transportation information. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BusRepository busRepository;

    public UserService(UserRepository userRepository, BusRepository busRepository) {
        this.userRepository = userRepository;
        this.busRepository = busRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listStudents() {
        return userRepository.findByRoleOrderByNameAsc(Role.STUDENT).stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse assignBus(Long studentId, AssignBusRequest req) {
        User user = findStudent(studentId);
        if (req.busId() == null) {
            user.setBus(null);
        } else {
            user.setBus(busRepository.findById(req.busId())
                    .orElseThrow(() -> new BadRequestException("Selected bus does not exist")));
        }
        user.setBoardingStop(req.boardingStop());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        userRepository.delete(findStudent(studentId));
    }

    private User findStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("Only student accounts can be changed here");
        }
        return user;
    }
}
