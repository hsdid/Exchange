package org.exchange.modules.engine.domain.model;

public enum EventType {
    ORDER_NEW((byte) 1),
    ORDER_CANCEL((byte) 2),
    BALANCE_DEPOSIT((byte) 3),
    BALANCE_WITHDRAW((byte) 4);

    private final byte code;

    EventType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static EventType fromCode(byte code) {
        for (EventType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EventType code: " + code);
    }
}
