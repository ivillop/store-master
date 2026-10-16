package com.storemaster.service;

import com.storemaster.config.AppProperties.MatchMode;

public final class LikePattern {

    private LikePattern() {
    }

    public static String of(String term, MatchMode mode) {
        String escaped = term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return mode == MatchMode.CONTAINS ? "%" + escaped + "%" : escaped;
    }
}
