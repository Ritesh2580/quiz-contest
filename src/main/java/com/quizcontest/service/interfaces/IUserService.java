package com.quizcontest.service.interfaces;

import com.quizcontest.dto.CreateUserRequest;
import com.quizcontest.dto.LoginRequest;
import com.quizcontest.dto.LoginResponse;
import com.quizcontest.dto.UserDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface for User Service
 * Follows Dependency Inversion Principle
 */
public interface IUserService {

    UserDTO createUser(CreateUserRequest request);

    LoginResponse login(LoginRequest request);

    UserDTO getUserById(UUID id);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(UUID id, CreateUserRequest request);

    void deleteUser(UUID id);
}
