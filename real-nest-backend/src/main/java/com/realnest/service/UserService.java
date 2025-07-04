package com.realnest.service;

import com.realnest.entity.User;
import com.realnest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String fullName, String username, String password,
            String email, String phone, List<String> roles) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setActive(true);

        Set<String> roleSet = new HashSet<>(roles);
        user.setRoles(roleSet);

        User savedUser = userRepository.save(user);
        System.out.println("User saved successfully with ID: " + savedUser.getId());
        return savedUser;
    }

    public User authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
