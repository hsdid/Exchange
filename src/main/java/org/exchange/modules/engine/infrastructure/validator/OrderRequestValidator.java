package org.exchange.modules.engine.infrastructure.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.infrastructure.cache.InstrumentCache;
import org.exchange.modules.engine.infrastructure.dto.OrderRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderRequestValidator implements ConstraintValidator<ValidOrder, OrderRequest> {
    private final InstrumentCache instrumentCache;

    public OrderRequestValidator(
            InstrumentCache instrumentCache
    ) {
        this.instrumentCache = instrumentCache;
    }
    @Override
    public boolean isValid(OrderRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        Instrument instrument = instrumentCache.getBySymbol(request.symbol());

        if (instrument == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Instrument not found")
                    .addPropertyNode( "symbol")
                    .addConstraintViolation();
            return false;
        }

        // validate amount
        if (request.amount().compareTo(instrument.getMinAmount()) < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Amount must be greater than " + instrument.getMinAmount())
                    .addPropertyNode("amount")
                    .addConstraintViolation();
            return false;
        }

        // validate tickSize
        BigDecimal remainder = request.price().remainder(instrument.getTickSize());
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Price must be multiple of tick size " + instrument.getTickSize())
                    .addPropertyNode("price")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
