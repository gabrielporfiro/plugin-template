package com.monk.asura.util;

import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

/**
 * Mensagens formatadas com a API {@link Message} do Hytale (sem códigos § legados).
 */
public final class MonkMessages {

    private static final String PREFIX = "[Monk] ";
    private static final String COLOR_PREFIX = "#55C8FF";
    private static final String COLOR_BODY = "#E8EEF8";
    private static final String COLOR_WARN = "#FF8888";
    private static final String COLOR_MUTED = "#AABBCC";

    private MonkMessages() {
    }

    @Nonnull
    public static Message prefix() {
        return Message.raw(PREFIX).color(COLOR_PREFIX).bold(true);
    }

    @Nonnull
    public static Message skill(@Nonnull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_BODY));
    }

    @Nonnull
    public static Message warning(@Nonnull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_WARN));
    }

    @Nonnull
    public static Message info(@Nonnull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_MUTED));
    }
}
