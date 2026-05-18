package com.monk.asura.visual;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.task.TaskRegistration;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.combo.MonkComboComponent;
import com.monk.asura.combo.MonkComboPhase;
import com.monk.asura.config.MonkAsuraConfig;
import com.monk.asura.util.MonkTasks;
import com.monk.asura.util.PlayerContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Atualiza órbita visual das esferas e partículas periódicas.
 */
public class OrbVisualSystem {

    private final MonkAsuraPlugin plugin;
    private final Map<UUID, Long> lastOrbParticleMs = new ConcurrentHashMap<>();
    @Nullable
    private TaskRegistration tickTask;

    public OrbVisualSystem(@Nonnull MonkAsuraPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        tickTask = MonkTasks.scheduleAtFixedRate(
            plugin,
            this::tickAll,
            100,
            100,
            TimeUnit.MILLISECONDS
        );
    }

    public void stop() {
        tickTask = null;
        lastOrbParticleMs.clear();
    }

    private void tickAll() {
        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            if (!playerRef.isValid()) {
                continue;
            }
            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) {
                continue;
            }
            world.execute(() -> {
                PlayerContext ctx = PlayerContext.from(playerRef);
                if (ctx != null) {
                    tickPlayer(ctx);
                }
            });
        }
    }

    private void tickPlayer(@Nonnull PlayerContext ctx) {
        MonkComboComponent state = plugin.getComboService().getOrCreate(ctx.playerRef().getUuid());
        plugin.getComboService().refreshFuryExpiry(state);

        if (state.getPhase() == MonkComboPhase.ASURA_CHARGING) {
            return;
        }

        if (state.getOrbCount() <= 0) {
            return;
        }

        MonkAsuraConfig config = plugin.getConfig();
        state.setOrbitAngle(state.getOrbitAngle() + config.getOrbitSpeed() * 0.05);

        float converge = state.getPhase() == MonkComboPhase.NORMAL ? 0f : 0f;
        if (state.getAsuraConvergeProgress() > 0f) {
            converge = state.getAsuraConvergeProgress();
        }

        double radius = config.getOrbitRadius() * (1.0 - converge);
        Vector3d center = ctx.playerRef().getTransform().getPosition();
        int max = config.getMaxOrbs();

        long now = System.currentTimeMillis();
        UUID id = ctx.playerRef().getUuid();
        long last = lastOrbParticleMs.getOrDefault(id, 0L);
        boolean spawnParticle = now - last > 200;

        for (int i = 0; i < state.getOrbCount(); i++) {
            double angle = state.getOrbitAngle() + (Math.PI * 2.0 * i / max);
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            double y = center.getY() + 1.0 + Math.sin(angle * 2) * 0.15;
            if (spawnParticle) {
                Vector3d orbPos = new Vector3d(x, y, z);
                MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ORB_IDLE, orbPos);
                if (i == 0 || now % 600 < 120) {
                    MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ELECTRIC_SPARKS, orbPos);
                }
            }
        }

        if (spawnParticle) {
            lastOrbParticleMs.put(id, now);
        }

        if (state.isFuryActive() && now % 800 < 120) {
            MonkVfxUtil.playFuryLoop(ctx);
        }
    }

    /**
     * Partículas imediatas na órbita ao criar um orbe (Habilidade 1).
     */
    public void burstOrbCreated(@Nonnull PlayerContext ctx, int orbIndex) {
        MonkComboComponent state = plugin.getComboService().getOrCreate(ctx.playerRef().getUuid());
        MonkAsuraConfig config = plugin.getConfig();
        int max = config.getMaxOrbs();

        Vector3d center = ctx.playerRef().getTransform().getPosition();
        double radius = config.getOrbitRadius();
        double angle = state.getOrbitAngle() + (Math.PI * 2.0 * (orbIndex - 1) / max);

        Vector3d orbitPos = new Vector3d(
            center.getX() + Math.cos(angle) * radius,
            center.getY() + 1.15 + Math.sin(angle * 2) * 0.15,
            center.getZ() + Math.sin(angle) * radius
        );

        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ORB_ELECTRIC_BURST, orbitPos);
        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ORB_SPAWN, orbitPos);
        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ORB_IDLE, orbitPos);
        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ELECTRIC_SPARKS, orbitPos);
    }

    public void clearPlayer(@Nonnull UUID playerId) {
        lastOrbParticleMs.remove(playerId);
    }
}
