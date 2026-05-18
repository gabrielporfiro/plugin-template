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
 * Esferas de energia em órbita horizontal: distância fixa, seguem o jogador a cada tick.
 */
public class OrbVisualSystem {

    private static final long ORB_PARTICLE_INTERVAL_MS = 90;

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
            50,
            50,
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

        if (state.getPhase() == MonkComboPhase.ASURA_CHARGING || state.getOrbCount() <= 0) {
            return;
        }

        MonkAsuraConfig config = plugin.getConfig();
        state.setOrbitAngle(state.getOrbitAngle() + config.getOrbitSpeed() * 0.025);

        float converge = state.getAsuraConvergeProgress() > 0f ? state.getAsuraConvergeProgress() : 0f;
        double radius = config.getOrbitRadius() * (1.0 - converge);
        Vector3d center = ctx.playerRef().getTransform().getPosition();
        int max = config.getMaxOrbs();

        long now = System.currentTimeMillis();
        UUID id = ctx.playerRef().getUuid();
        if (now - lastOrbParticleMs.getOrDefault(id, 0L) < ORB_PARTICLE_INTERVAL_MS) {
            return;
        }
        lastOrbParticleMs.put(id, now);

        for (int i = 0; i < state.getOrbCount(); i++) {
            Vector3d orbPos = OrbOrbitUtil.computeOrbPosition(
                center,
                radius,
                state.getOrbitAngle(),
                i,
                max
            );
            MonkVfxUtil.spawnOrbSphere(ctx, orbPos);
        }

        if (state.isFuryActive() && now % 800 < 90) {
            MonkVfxUtil.playFuryLoop(ctx);
        }
    }

    public void burstOrbCreated(@Nonnull PlayerContext ctx, int orbIndex) {
        MonkComboComponent state = plugin.getComboService().getOrCreate(ctx.playerRef().getUuid());
        MonkAsuraConfig config = plugin.getConfig();
        int max = config.getMaxOrbs();

        Vector3d center = ctx.playerRef().getTransform().getPosition();
        Vector3d orbitPos = OrbOrbitUtil.computeOrbPosition(
            center,
            config.getOrbitRadius(),
            state.getOrbitAngle(),
            orbIndex - 1,
            max
        );

        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ORB_ELECTRIC_BURST, orbitPos);
        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ORB_SPAWN, orbitPos);
        MonkVfxUtil.spawnOrbSphere(ctx, orbitPos);
    }

    public void clearPlayer(@Nonnull UUID playerId) {
        lastOrbParticleMs.remove(playerId);
    }
}
