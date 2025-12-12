package org.exchange.modules.engine.domain;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class DeduplicationChecker {
    private static final int MAX_SIZE = 100_000;

    private final Set<String> processedIds = Collections.newSetFromMap(
            new LinkedHashMap<String, Boolean>(MAX_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > MAX_SIZE;
                }
            }
    );

    public boolean isDuplicate(String clientOrderId) {
        return processedIds.contains(clientOrderId);
    }

    public void markAsProcessed(String clientOrderId) {
        processedIds.add(clientOrderId);
    }
}
