package com.priyonix.crm.controller;

import com.priyonix.crm.dto.UserResponse;
import com.priyonix.crm.entity.User;
import com.priyonix.crm.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class UserController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager) {

        this.userService = userService;

        this.authenticationManager =
                authenticationManager;
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody User user) {

        // ==========================
        // PASSWORD VALIDATION
        // ==========================

        String password = user.getPassword();

        if (password == null || password.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password is required."
                            )
                    );
        }

        if (password.length() < 8) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password must contain at least 8 characters."
                            )
                    );
        }

        if (!password.matches(".*[A-Z].*")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password must contain at least one uppercase letter."
                            )
                    );
        }

        if (!password.matches(".*[a-z].*")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password must contain at least one lowercase letter."
                            )
                    );
        }

        if (!password.matches(".*[0-9].*")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password must contain at least one number."
                            )
                    );
        }

        if (!password.matches(".*[@$!%*?&#].*")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password must contain at least one special character."
                            )
                    );
        }

        // ==========================
        // CREATE USER
        // ==========================

        User createdUser =
                userService.createUser(user);

        return ResponseEntity.ok(
                new UserResponse(createdUser)
        );
    }

    // =========================================================
    // GET ALL USERS
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> users =
                userService
                        .getAllUsers()
                        .stream()
                        .map(UserResponse::new)
                        .toList();

        return ResponseEntity.ok(users);
    }

    // =========================================================
    // GET CURRENT LOGGED-IN USER
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return userService
                .getUserByEmail(authentication.getName())
                .map(user ->
                        ResponseEntity.ok(
                                new UserResponse(user)
                        )
                )
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // GET ACTIVE USERS
    // USED BY CRM DROPDOWNS
    // =========================================================

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {

        List<UserResponse> users =
                userService
                        .getAllUsers()
                        .stream()
                        .filter(User::isActive)
                        .map(UserResponse::new)
                        .toList();

        return ResponseEntity.ok(users);
    }

    // =========================================================
    // GET USER BY ID
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return userService
                .getUserById(id)
                .map(user ->
                        ResponseEntity.ok(
                                new UserResponse(user)
                        )
                )
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // GET USER BY EMAIL
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email) {

        return userService
                .getUserByEmail(email)
                .map(user ->
                        ResponseEntity.ok(
                                new UserResponse(user)
                        )
                )
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email =
                loginRequest.get("email");

        String password =
                loginRequest.get("password");

        // ==========================
        // VALIDATE INPUT
        // ==========================

        if (email == null ||
                password == null ||
                email.isBlank() ||
                password.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Email and password are required"
                            )
                    );
        }

        try {

            // ==========================
            // AUTHENTICATE USER
            // ==========================

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    password
                            )
                    );

            // ==========================
            // CREATE SECURITY CONTEXT
            // ==========================

            SecurityContext context =
                    SecurityContextHolder
                            .createEmptyContext();

            context.setAuthentication(
                    authentication
            );

            SecurityContextHolder
                    .setContext(context);

            // ==========================
            // SAVE SESSION
            // ==========================

            securityContextRepository.saveContext(
                    context,
                    request,
                    response
            );

            // ==========================
            // GET USER
            // ==========================

            User user =
                    userService
                            .getUserByEmail(email)
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "User not found"
                                            )
                            );

            // ==========================
            // RETURN SAFE RESPONSE
            // ==========================

            return ResponseEntity.ok(
                    new UserResponse(user)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid email or password"
                            )
                    );
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        if (request.getSession(false) != null) {

            request.getSession(false)
                    .invalidate();
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logged out successfully"
                )
        );
    }

    // =========================================================
    // UPDATE USER
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        User updatedUser =
                userService.updateUser(
                        id,
                        user
                );

        return ResponseEntity.ok(
                new UserResponse(updatedUser)
        );
    }

    // =========================================================
    // DELETE USER
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}