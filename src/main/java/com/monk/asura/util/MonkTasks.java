package com.monk.asura.util;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.task.TaskRegistration;
import com.monk.asura.MonkAsuraPlugin;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tarefas agendadas registradas no {@link com.hypixel.hytale.server.core.task.TaskRegistry}
 * do plugin, para cancelamento automático no shutdown (recomendado pela documentação).
 */
public final class MonkTasks {

    private MonkTasks() {
    }

    @Nonnull
    public static TaskRegistration schedule(
        @Nonnull MonkAsuraPlugin plugin,
        @Nonnull Runnable task,
        long delay,
        @Nonnull TimeUnit unit
    ) {
        ScheduledFuture<?> future = HytaleServer.SCHEDULED_EXECUTOR.schedule(task, delay, unit);
        return plugin.getTaskRegistry().registerTask(future);
    }

    @Nonnull
    public static TaskRegistration scheduleAtFixedRate(
        @Nonnull MonkAsuraPlugin plugin,
        @Nonnull Runnable task,
        long initialDelay,
        long period,
        @Nonnull TimeUnit unit
    ) {
        ScheduledFuture<?> future = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
            task,
            initialDelay,
            period,
            unit
        );
        return plugin.getTaskRegistry().registerTask(future);
    }
}
