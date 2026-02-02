package org.exchange.modules.user.infrastructure.rest;

import org.exchange.modules.core.domain.message.CommandBus;
import org.exchange.modules.user.application.command.RegisterUserCommand;
import org.exchange.modules.user.infrastructure.rest.dto.auth.AuthenticationResponse;
import org.exchange.modules.user.infrastructure.rest.dto.auth.LoginRequest;
import org.exchange.modules.user.infrastructure.rest.dto.auth.RegisterRequest;
import org.exchange.modules.user.infrastructure.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CommandBus commandBus;

    public AuthController(
            AuthenticationService authenticationService,
            CommandBus commandBus
    ) {
        this.authenticationService = authenticationService;
        this.commandBus = commandBus;
    }

    @PostMapping("/register")
    public ResponseEntity<Long> register(
            @RequestBody RegisterRequest request
    ) {
        var command = new RegisterUserCommand(
                request.fullName(),
                request.email(),
                request.password()
        );
        Long userId = commandBus.dispatch(command);

        return ResponseEntity.ok(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
