package com.monk.asura.debug;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Debug NDJSON para sessão 247ab7 (modo debug).
 */
public final class MonkDebugLog {

    private static final String SESSION_ID = "247ab7";
    private static final Path[] LOG_PATHS = {
        Path.of("c:/pessoal/2026.03.26-89796e57b/debug-247ab7.log"),
        Path.of("debug-247ab7.log"),
        Path.of("../debug-247ab7.log"),
    };

    private MonkDebugLog() {
    }

    public static void log(
        @Nonnull String hypothesisId,
        @Nonnull String location,
        @Nonnull String message,
        @Nonnull Map<String, ?> data
    ) {
        // #region agent log
        try {
            long ts = System.currentTimeMillis();
            String dataJson = data.entrySet().stream()
                .map(e -> "\"" + escape(e.getKey()) + "\":" + toJsonValue(e.getValue()))
                .collect(Collectors.joining(","));
            String line = "{\"sessionId\":\"" + SESSION_ID + "\",\"hypothesisId\":\""
                + escape(hypothesisId) + "\",\"location\":\"" + escape(location)
                + "\",\"message\":\"" + escape(message) + "\",\"data\":{" + dataJson
                + "},\"timestamp\":" + ts + "}\n";
            byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
            for (Path path : LOG_PATHS) {
                try {
                    Path parent = path.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    break;
                } catch (Exception ignored) {
                    // try next path
                }
            }
        } catch (Exception ignored) {
            // debug only
        }
        // #endregion
    }

    @Nonnull
    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String toJsonValue(Object v) {
        if (v == null) {
            return "null";
        }
        if (v instanceof Boolean b) {
            return b.toString();
        }
        if (v instanceof Number) {
            return v.toString();
        }
        return "\"" + escape(String.valueOf(v)) + "\"";
    }
}
