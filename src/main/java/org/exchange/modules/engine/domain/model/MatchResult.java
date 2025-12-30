package org.exchange.modules.engine.domain.model;

import java.util.List;

public record MatchResult(List<TradeMatch> trades, Order originalOrder) {}
