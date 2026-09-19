package com.vidasalud.report.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RangeParserTest {

    @Test
    void parseaHoras() {
        assertThat(RangeParser.parse("last24h")).isEqualTo(Duration.ofHours(24));
        assertThat(RangeParser.parse("last1h")).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void parseaDias() {
        assertThat(RangeParser.parse("last7d")).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void caeAlDefaultSiEsInvalidoOVacio() {
        assertThat(RangeParser.parse(null)).isEqualTo(Duration.ofHours(24));
        assertThat(RangeParser.parse("")).isEqualTo(Duration.ofHours(24));
        assertThat(RangeParser.parse("cualquier-cosa")).isEqualTo(Duration.ofHours(24));
    }
}
