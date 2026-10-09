package com.priyonix.crm.service;

import com.priyonix.crm.entity.User;
import com.priyonix.crm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==============================
    // PASSWORD VALIDATION
    // ==============================

    private void validatePassword(String password) {

        if (password == null || password.isBlank()) {
            throw new RuntimeException(
                    "Password is required."
            );
        }

        if (password.length() < 8) {
            throw new RuntimeException(
                    "Password must contain at least 8 characters."
            );
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException(
                    "Password must contain at least one uppercase letter."
            );
        }

        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException(
                    "Password must contain at least one lowercase letter."
            );
        }

        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException(
                    "Password must contain at least one number."
            );
        }

        if (!password.matches(".*[@$!%*?&#].*")) {
            throw new RuntimeException(
                    "Password must contain at least one special character."
            );
        }
    }


    // ==============================
    // CREATE USER
    // ==============================

    public User createUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException(
                    "Email already exists"
            );
        }

        // Validate password before hashing
        validatePassword(user.getPassword());

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        return userRepository.save(user);
    }


    // ==============================
    // GET ALL USERS
    // ==============================

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    // ==============================
    // GET USER BY ID
    // ==============================

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    // ==============================
    // GET USER BY EMAIL
    // ==============================

    public Optional<User> getUserByEmail(String email) {

        Optional<User> userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        /*
         * Existing users may have passwords stored
         * before BCrypt was implemented.
         *
         * BCrypt passwords normally start with:
         * $2a$, $2b$ or $2y$
         *
         * If the existing password is not BCrypt,
         * convert it to BCrypt automatically.
         */

        String password = user.getPassword();

        if (password != null &&
                !password.startsWith("$2a$") &&
                !password.startsWith("$2b$") &&
                !password.startsWith("$2y$")) {

            user.setPassword(
                    passwordEncoder.encode(password)
            );

            user = userRepository.save(user);
        }

        return Optional.of(user);
    }


    // ==============================
    // VERIFY PASSWORD
    // ==============================

    public boolean verifyPassword(
            String rawPassword,
            String encodedPassword) {

        return passwordEncoder.matches(
                rawPassword,
                encodedPassword
        );
    }


    // ==============================
    // UPDATE USER
    // ==============================

    public User updateUser(
            Long id,
            User updatedUser) {

        User existingUser =
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        existingUser.setName(
                updatedUser.getName()
        );

        existingUser.setEmail(
                updatedUser.getEmail()
        );

        existingUser.setPhone(
                updatedUser.getPhone()
        );

        existingUser.setRole(
                updatedUser.getRole()
        );

        existingUser.setActive(
                updatedUser.isActive()
        );


        /*
         * Only change the password when
         * a new password is provided.
         */

        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isBlank()) {

            // Validate new password
            validatePassword(
                    updatedUser.getPassword()
            );

            // Hash new password
            existingUser.setPassword(
                    passwordEncoder.encode(
                            updatedUser.getPassword()
                    )
            );
        }

        return userRepository.save(existingUser);
    }


    // ==============================
    // DELETE USER
    // ==============================

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException(
                    "User not found"
            );
        }

        userRepository.deleteById(id);
    }
}