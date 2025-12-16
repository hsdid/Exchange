package org.exchange.modules.engine.domain.model;

/**
 * Trading status for an instrument.
 */
public enum InstrumentStatus {
    ACTIVE,      // Normal trading
    HALTED,      // Trading suspended (emergency)
    MAINTENANCE, // Scheduled maintenance
    DELISTED     // No longer tradable
}
