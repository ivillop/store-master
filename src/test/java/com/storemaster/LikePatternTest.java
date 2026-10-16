package com.storemaster;

import com.storemaster.config.AppProperties.MatchMode;
import com.storemaster.service.LikePattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LikePatternTest {

    @Test
    void exactModeKeepsTermWithoutWildcards() {
        assertEquals("Jawa Barat", LikePattern.of("Jawa Barat", MatchMode.EXACT));
    }

    @Test
    void containsModeWrapsTermWithWildcards() {
        assertEquals("%Jawa%", LikePattern.of("Jawa", MatchMode.CONTAINS));
    }

    @Test
    void userSuppliedWildcardsAreEscaped() {
        assertEquals("!%!_!!", LikePattern.of("%_!", MatchMode.EXACT));
    }
}
