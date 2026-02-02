package org.exchange.modules.user.application.commandHandler;

import org.exchange.modules.core.domain.message.Command;
import org.exchange.modules.core.domain.message.CommandHandler;
import org.exchange.modules.user.application.command.RegisterUserCommand;
import org.exchange.modules.user.domain.Role;
import org.exchange.modules.user.domain.User;
import org.exchange.modules.user.infrastructure.db.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserHandler implements CommandHandler<Long, RegisterUserCommand> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserHandler(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long handle(RegisterUserCommand command) {
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        var user = new User(
                command.fullName(),
                command.email(),
                passwordEncoder.encode(command.password()),
                Role.USER
        );

        return userRepository.save(user).getId();
    }
}
