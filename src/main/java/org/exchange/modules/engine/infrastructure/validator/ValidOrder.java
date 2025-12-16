package org.exchange.modules.engine.infrastructure.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderRequestValidator.class)
public @interface ValidOrder {
    String message() default "Invalid order parameters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
