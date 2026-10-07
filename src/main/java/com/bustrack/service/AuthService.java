package com.bustrack.service;

import com.bustrack.dto.Dtos.*;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Bus;
import com.bustrack.model.Role;
import com.bustrack.model.User;
import com.bustrack.repository.BusRepository;
import com.bustrack.repository.UserRepository;
import com.bustrack.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, BusRepository busRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Public registration always creates a STUDENT account. */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.STUDENT);
        user.setRollNumber(req.rollNumber());
        user.setPhone(req.phone());
        user.setBoardingStop(req.boardingStop());
        user.setBus(findBus(req.busId()));
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()),
                UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()),
                UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserResponse updateProfile(String email, ProfileRequest req) {
        User user = currentUser(email);
        user.setName(req.name().trim());
        user.setPhone(req.phone());
        user.setRollNumber(req.rollNumber());
        user.setBoardingStop(req.boardingStop());
        user.setBus(findBus(req.busId()));
        return UserResponse.from(userRepository.save(user));
    }

    private Bus findBus(Long busId) {
        if (busId == null) return null;
        return busRepository.findById(busId)
                .orElseThrow(() -> new BadRequestException("Selected bus does not exist"));
    }
}
