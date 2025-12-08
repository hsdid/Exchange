package org.exchange.modules.engine.domain;

import org.exchange.modules.engine.infrastructure.dto.OrderCommand;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryOrderSerializer {

    // Bufor 1KB wystarczy z zapasem na jedno zlecenie
    // W produkcji oblicza się to precyzyjniej
    private static final int MAX_RECORD_SIZE = 1024;

    public static void serialize(OrderCommand cmd, ByteBuffer buffer) {
        // 1. Zapamiętujemy pozycję startową, żeby potem wpisać długość
        int startPos = buffer.position();

        // Zostawiamy 4 bajty miejsca na "Długość całego rekordu"
        buffer.putInt(0);

        // ZMIANA: Zapisujemy Longa (8 bajtów) zamiast Stringa
        buffer.putLong(cmd.userId() != null ? cmd.userId() : 0L);

        writeString(buffer, cmd.clientOrderId());
        buffer.put((byte) (cmd.side() == Side.BUY ? 0 : 1));
        writeString(buffer, cmd.symbol());
        writeString(buffer, cmd.amount().toString());
        writeString(buffer, cmd.price().toString());

        // 3. Obliczamy faktyczną długość
        int endPos = buffer.position();
        int dataLength = endPos - startPos - 4; // -4 bo nie liczymy samego nagłówka długości

        buffer.putInt(startPos, dataLength);
        buffer.position(endPos);
    }

    public static OrderCommand deserialize(ByteBuffer buffer) {
        Long userId = buffer.getLong();

        String reqId = readString(buffer);
        byte sideByte = buffer.get();
        Side side = (sideByte == 0) ? Side.BUY : Side.SELL;
        String symbol = readString(buffer);
        BigDecimal amount = new BigDecimal(readString(buffer));
        BigDecimal price = new BigDecimal(readString(buffer));

        return new OrderCommand(reqId, userId, side, symbol, amount, price);
    }

    // --- Helpery do Stringów ---

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
