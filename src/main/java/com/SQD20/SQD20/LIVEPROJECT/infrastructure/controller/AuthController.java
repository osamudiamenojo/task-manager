package com.SQD20.SQD20.LIVEPROJECT.infrastructure.controller;

import com.SQD20.SQD20.LIVEPROJECT.domain.entites.AppUser;
import com.SQD20.SQD20.LIVEPROJECT.infrastructure.config.JwtService;
import com.SQD20.SQD20.LIVEPROJECT.payload.request.AuthenticationRequest;
import com.SQD20.SQD20.LIVEPROJECT.payload.request.RegisterRequest;
import com.SQD20.SQD20.LIVEPROJECT.payload.response.RegisterResponse;
import com.SQD20.SQD20.LIVEPROJECT.payload.response.AuthenticationResponse;
import com.SQD20.SQD20.LIVEPROJECT.service.impl.UserServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserServiceImpl userService;

    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated
            @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) throws MessagingException, JsonProcessingException {

        if (bindingResult.hasErrors()) {
            // Construct error response with specific error messages for each field
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        RegisterResponse authenticationResponse = userService.register(registerRequest);
        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(userService.authenticate(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam(name = "token") String token ){
        return ResponseEntity.ok(userService.verifyEmail(token));
    }



    @PostMapping("/users/resend-email")
    public ResponseEntity<?> resendEmailVerification(@RequestParam(name = "email") String email) {
        ResponseEntity<?> response = userService.resendEmailVerification(email);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email,  String oldPassword,@RequestHeader String newPassword){
        return new ResponseEntity<>(userService.resetPassword(email,oldPassword, newPassword), HttpStatus.OK);
    }

    @PostMapping("/forgot-password-email")
    public ResponseEntity<?> forgotPasswordEmail(@RequestParam String email){
        ResponseEntity<?> response = userService.forgotPasswordEmail(email);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/verify-forgot-password-email")
    public String verifyForgotPasswordEmail(@RequestParam("resetToken") String token) throws IOException {
        return userService.verifyForgotPasswordEmail(token);
    }

    @PostMapping("/reset-forgot-password")
    public String resetForgotPassword(@RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam("email") String email) {
        return userService.forgotPassword(email,newPassword, confirmPassword);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshTokenHeader) {

        String username = jwtService.extractUsernameFromToken(refreshTokenHeader);

        UserDetails userDetails = userService.loadUserByUsername(username);

        String newAccessToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(newAccessToken);
    }
    @PostMapping("/logout")
    public String logout() {
       return userService.logout();
    }



}
