package org.exchange.modules.user.infrastructure;

import org.exchange.modules.user.infrastructure.rest.dto.auth.AuthenticationResponse;
import org.exchange.modules.user.infrastructure.rest.dto.auth.LoginRequest;
import org.exchange.modules.user.infrastructure.rest.dto.auth.RegisterRequest;
import org.exchange.modules.user.domain.Role;
import org.exchange.modules.user.domain.User;
import org.exchange.modules.user.infrastructure.db.repository.UserRepository;
import org.exchange.infrastructure.config.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        var user = new User(request.fullName(), request.email(), passwordEncoder.encode(request.password()), Role.USER);
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
