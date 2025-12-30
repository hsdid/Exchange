package org.exchange.modules.engine.domain;

import org.exchange.modules.engine.domain.journal.JournalModelEvent;
import org.exchange.modules.engine.domain.model.Deposit;
import org.exchange.modules.engine.domain.model.EventType;
import org.exchange.modules.engine.domain.model.Order;
import org.exchange.modules.engine.domain.model.Side;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryEventSerializer {

    private static final int MAX_RECORD_SIZE = 1024;

    public static void serialize(JournalModelEvent event, ByteBuffer buffer) {
        // 1. Zapamiętujemy pozycję startową, żeby potem wpisać długość
        int startPos = buffer.position();
        // Zostawiamy 4 bajty miejsca na "Długość całego rekordu"
        buffer.putInt(0);

        if (event instanceof Order order) {
            buffer.put(EventType.ORDER_NEW.getCode()); // Typ: ORDER_NEW

            buffer.putLong(order.getUserId() != null ? order.getUserId() : 0L);
            writeString(buffer, order.getClientOrderId());
            buffer.put((byte) (order.getSide() == Side.BUY ? 0 : 1));
            buffer.putLong(order.getInstrumentId());
            writeString(buffer, order.getAmount().toString());
            writeString(buffer, order.getPrice().toString());
        } else if (event instanceof Deposit deposit) {
            buffer.put(EventType.BALANCE_DEPOSIT.getCode()); // Typ: BALANCE_DEPOSIT

            buffer.putLong(deposit.getUserId());
            buffer.putLong(deposit.getAssetId());
            writeString(buffer, deposit.getAmount().toString());
        }

        // Obliczamy faktyczną długość
        int endPos = buffer.position();
        int dataLength = endPos - startPos - 4;
        buffer.putInt(startPos, dataLength);
        buffer.position(endPos);
    }

    public static JournalModelEvent deserialize(ByteBuffer buffer) {
       // Odczytaj typ (to jest ten 1 bajt po długości)
        EventType type = EventType.fromCode(buffer.get());

        return switch (type) {
            case ORDER_NEW -> deserializeOrder(buffer);
            case BALANCE_DEPOSIT -> deserializeDeposit(buffer);
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }

    private static Order deserializeOrder(ByteBuffer buffer) {
        Long userId = buffer.getLong();
        String reqId = readString(buffer);
        byte sideByte = buffer.get();
        Side side = (sideByte == 0) ? Side.BUY : Side.SELL;
        Long instrumentId = buffer.getLong(); // Changed: read instrumentId instead of symbol string
        BigDecimal amount = new BigDecimal(readString(buffer));
        BigDecimal price = new BigDecimal(readString(buffer));

        return new Order(reqId, userId, side, instrumentId, amount, price);
    }

    private static Deposit deserializeDeposit(ByteBuffer buffer) {
        Long userId = buffer.getLong();
        Long assetId = buffer.getLong();
        BigDecimal amount = new BigDecimal(readString(buffer));

        return new Deposit(userId, assetId, amount);
    }

    private static void writeString(ByteBuffer buf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buf.putInt(bytes.length); // Najpierw długość
        buf.put(bytes);           // Potem treść
    }

    private static String readString(ByteBuffer buf) {
        int length = buf.getInt();
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
