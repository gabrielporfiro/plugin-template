package com.monk.asura.visual;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.monk.asura.config.MonkAsuraConfig;
import com.monk.asura.util.MonkMessages;
import com.monk.asura.util.PlayerContext;

import javax.annotation.Nonnull;
import java.util.List;

public final class MonkVfxUtil {

    public static final String PARTICLE_ORB_SPAWN = "Monk_SpiritOrb_Spawn";
    public static final String PARTICLE_ORB_IDLE = "Monk_SpiritOrb_Idle";
    public static final String PARTICLE_ORB_BODY_AURA = "Monk_Orb_BodyAura";
    public static final String PARTICLE_ELECTRIC_SPARKS = "Monk_Electric_Sparks";
    public static final String PARTICLE_ORB_ELECTRIC_BURST = "Monk_Orb_ElectricBurst";
    /** Partículas vanilla de raio (fallback). */
    public static final String PARTICLE_VANILLA_LIGHTNING = "Lightning";
    public static final String PARTICLE_FURY_AURA = "Monk_Fury_Aura";
    public static final String PARTICLE_FURY_LIGHTNING = "Monk_Fury_Lightning";
    public static final String PARTICLE_ASURA_CHARGE = "Monk_Asura_Charge";
    public static final String PARTICLE_ASURA_IMPACT = "Monk_Asura_Impact";
    public static final String PARTICLE_ASURA_EXPLOSION = "Monk_Asura_Explosion";

    private static final String FALLBACK_PARTICLE = "Dust_Sparkles_Fine";

    private MonkVfxUtil() {
    }

    /**
     * Envia partículas ao jogador e a quem estiver perto. Nunca use lista vazia —
     * {@link ParticleUtil} só envia pacotes aos refs da lista (ou coleta espacial no overload sem ref).
     */
    public static void spawnAt(@Nonnull PlayerContext ctx, @Nonnull String systemId, @Nonnull Vector3d position) {
        try {
            ParticleUtil.spawnParticleEffect(systemId, position, ctx.store());
        } catch (Exception primary) {
            try {
                ParticleUtil.spawnParticleEffect(FALLBACK_PARTICLE, position, ctx.store());
            } catch (Exception fallback) {
                try {
                    ParticleUtil.spawnParticleEffect(
                        FALLBACK_PARTICLE,
                        position,
                        List.of(ctx.entityRef()),
                        ctx.store()
                    );
                } catch (Exception ignored) {
                    // sem VFX
                }
            }
        }
    }

    /** Esfera redonda + faíscas na casca (órbita). */
    public static void spawnOrbSphere(@Nonnull PlayerContext ctx, @Nonnull Vector3d position) {
        spawnAt(ctx, PARTICLE_ORB_IDLE, position);
    }

    public static void spawnOnPlayer(@Nonnull PlayerContext ctx, @Nonnull String systemId) {
        spawnAt(ctx, systemId, ctx.playerRef().getTransform().getPosition());
    }

    /**
     * Faíscas elétricas ao redor do corpo e à frente (mão estendida).
     */
    public static void spawnElectricAroundPlayer(@Nonnull PlayerContext ctx) {
        Vector3d center = ctx.playerRef().getTransform().getPosition();
        Vector3f rotation = ctx.playerRef().getTransform().getRotation();
        Vector3d forward = ctx.playerRef().getTransform().getDirection();

        spawnOnPlayer(ctx, PARTICLE_ELECTRIC_SPARKS);
        spawnAt(ctx, PARTICLE_ELECTRIC_SPARKS, center);

        Vector3d handForward = new Vector3d(
            center.getX() + forward.getX() * 0.85,
            center.getY() + 1.25,
            center.getZ() + forward.getZ() * 0.85
        );
        spawnAt(ctx, PARTICLE_ORB_ELECTRIC_BURST, handForward);
        spawnAt(ctx, PARTICLE_ELECTRIC_SPARKS, handForward);

        trySpawnVanillaLightning(ctx, handForward);

        double yawRad = Math.toRadians(rotation.getY());
        for (int i = 0; i < 4; i++) {
            double angle = yawRad + (Math.PI * 2.0 * i / 4.0);
            Vector3d ring = new Vector3d(
                center.getX() + Math.cos(angle) * 1.1,
                center.getY() + 1.0 + (i % 2) * 0.3,
                center.getZ() + Math.sin(angle) * 1.1
            );
            spawnAt(ctx, PARTICLE_ELECTRIC_SPARKS, ring);
        }
    }

    private static void trySpawnVanillaLightning(@Nonnull PlayerContext ctx, @Nonnull Vector3d position) {
        try {
            ParticleUtil.spawnParticleEffect(PARTICLE_VANILLA_LIGHTNING, position, ctx.store());
        } catch (Exception ignored) {
            // asset vanilla opcional
        }
    }

    /**
     * Canalização: pose de uma mão à frente + eletricidade + orbe na órbita.
     */
    public static void spawnOrbCreation(
        @Nonnull PlayerContext ctx,
        int orbIndex,
        int maxOrbs,
        double orbitAngleRad,
        @Nonnull MonkAsuraConfig config
    ) {
        MonkAnimationUtil.playOrbCastAnimation(ctx);
        spawnElectricAroundPlayer(ctx);

        Vector3d orbitPos = OrbOrbitUtil.computeOrbPosition(
            ctx.playerRef().getTransform().getPosition(),
            config.getOrbitRadius(),
            orbitAngleRad,
            orbIndex - 1,
            maxOrbs
        );

        spawnOnPlayer(ctx, PARTICLE_ORB_SPAWN);
        spawnOnPlayer(ctx, PARTICLE_ORB_BODY_AURA);
        spawnAt(ctx, PARTICLE_ORB_ELECTRIC_BURST, orbitPos);
        spawnAt(ctx, PARTICLE_ORB_SPAWN, orbitPos);
        spawnOrbSphere(ctx, orbitPos);
        trySpawnVanillaLightning(ctx, orbitPos);
    }

    public static void notifyWarning(@Nonnull PlayerRef playerRef, @Nonnull String text) {
        playerRef.sendMessage(MonkMessages.warning(text));
        NotificationUtil.sendNotification(
            playerRef.getPacketHandler(),
            Message.raw(text),
            NotificationStyle.Warning
        );
    }

    public static void notifyInfo(@Nonnull PlayerRef playerRef, @Nonnull String text) {
        playerRef.sendMessage(MonkMessages.info(text));
        NotificationUtil.sendNotification(
            playerRef.getPacketHandler(),
            Message.raw(text),
            NotificationStyle.Default
        );
    }

    public static void notifySkill(@Nonnull PlayerRef playerRef, @Nonnull String text) {
        playerRef.sendMessage(MonkMessages.skill(text));
    }

    public static void showFuryTitle(@Nonnull PlayerRef playerRef) {
        EventTitleUtil.showEventTitleToPlayer(
            playerRef,
            Message.raw("FÚRIA!"),
            Message.raw("O poder interior foi liberado"),
            true,
            null,
            3f,
            0.8f,
            1.2f
        );
    }

    public static void showAsuraTitle(@Nonnull PlayerRef playerRef) {
        EventTitleUtil.showEventTitleToPlayer(
            playerRef,
            Message.raw("PUNHO SUPREMO DE ASURA"),
            Message.raw("O poder extremo foi liberado!"),
            true,
            null,
            4f,
            0.5f,
            1.5f
        );
    }

    public static void playFuryLoop(@Nonnull PlayerContext ctx) {
        spawnOnPlayer(ctx, PARTICLE_FURY_AURA);
        spawnOnPlayer(ctx, PARTICLE_FURY_LIGHTNING);
        spawnOnPlayer(ctx, PARTICLE_ELECTRIC_SPARKS);
    }
}
