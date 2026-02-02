package org.exchange.modules.core.infrastructure;

import org.exchange.modules.core.domain.message.Command;
import org.exchange.modules.core.domain.message.CommandBus;
import org.exchange.modules.core.domain.message.CommandHandler;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpringCommandBus implements CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<?, ? extends Command>> handlers = new HashMap<>();


    public SpringCommandBus(List<CommandHandler<?, ? extends Command>> handlerList) {
        handlerList.forEach(this::registerHandler);
    }

    private void registerHandler(CommandHandler<?, ? extends Command> handler) {
        Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);
        if (generics != null) {
            Class<? extends Command> commandClass = (Class<? extends Command>) generics[1];
            handlers.put(commandClass, handler);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Command, R> R dispatch(C command) {
        CommandHandler<R, C> handler = (CommandHandler<R, C>) handlers.get(command.getClass());
        if (null != handler) {
            return handler.handle(command);
        }
        throw new IllegalArgumentException("No handler found for command: " + command.getClass().getName());
    }
}
