package com.vidasalud.report.util;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsea los rangos usados por los endpoints de reporteria
 * (ver Caso VidaSalud: "range=last24h", "range=last7d").
 * Formato soportado: "last" + numero + unidad ('h' horas, 'd' dias).
 * Cualquier valor ausente o invalido cae al default (24 horas).
 */
public final class RangeParser {

    private static final Pattern PATTERN = Pattern.compile("^last(\\d+)([hd])$", Pattern.CASE_INSENSITIVE);
    private static final Duration DEFAULT_RANGE = Duration.ofHours(24);

    private RangeParser() {
    }

    public static Duration parse(String range) {
        if (range == null || range.isBlank()) {
            return DEFAULT_RANGE;
        }
        Matcher matcher = PATTERN.matcher(range.trim());
        if (!matcher.matches()) {
            return DEFAULT_RANGE;
        }
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();
        return "d".equals(unit) ? Duration.ofDays(amount) : Duration.ofHours(amount);
    }
}
