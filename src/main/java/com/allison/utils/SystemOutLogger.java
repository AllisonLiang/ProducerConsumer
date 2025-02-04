package com.allison.utils;

import java.text.MessageFormat;

public class SystemOutLogger {
    private final StringBuilder logBuilder = new StringBuilder();

    public void log(final String format, final Object... args) {
        logBuilder.setLength(0);
        logBuilder.append(MessageFormat.format(format, args));
        System.out.println(logBuilder);
    }
}
