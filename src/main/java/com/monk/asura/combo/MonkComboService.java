package com.monk.asura.combo;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.config.MonkAsuraConfig;
import com.monk.asura.ui.MonkSkillBarHud;
import com.monk.asura.util.PlayerContext;
import com.monk.asura.visual.MonkVfxUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MonkComboService {

    private static final String FURY_EFFECT_ID = "Monk_Fury";
    private static final String ORB_AURA_EFFECT_ID = "Monk_OrbAura";

    private final MonkAsuraPlugin plugin;
    private final Map<UUID, MonkComboComponent> states = new ConcurrentHashMap<>();
    private final Map<UUID, MonkSkillBarHud> huds = new ConcurrentHashMap<>();

    public MonkComboService(@Nonnull MonkAsuraPlugin plugin) {
        this.plugin = plugin;
    }

    @Nonnull
    public MonkComboComponent getOrCreate(@Nonnull UUID playerId) {
        return states.computeIfAbsent(playerId, MonkComboComponent::new);
    }

    public void removePlayer(@Nonnull UUID playerId) {
        states.remove(playerId);
        huds.remove(playerId);
        plugin.getOrbVisualSystem().clearPlayer(playerId);
    }

    public void clearAllHuds() {
        huds.clear();
    }

    public void detachHud(@Nonnull PlayerRef playerRef) {
        MonkSkillBarHud hud = huds.remove(playerRef.getUuid());
        if (hud == null || !playerRef.isValid()) {
            return;
        }
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world == null) {
            return;
        }
        try {
            world.execute(() -> {
                if (!playerRef.isValid()) {
                    return;
                }
                Ref<EntityStore> entityRef = playerRef.getReference();
                if (entityRef == null || !entityRef.isValid()) {
                    return;
                }
                Player player = world.getEntityStore().getStore()
                    .getComponent(entityRef, Player.getComponentType());
                if (player != null) {
                    player.getHudManager().setCustomHud(playerRef, null);
                }
            });
        } catch (Exception ignored) {
            // shutdown / mundo parando
        }
    }

    public boolean isHudEnabled(@Nonnull UUID playerId) {
        return huds.containsKey(playerId);
    }

    public void attachHud(@Nonnull PlayerContext ctx) {
        UUID playerId = ctx.playerRef().getUuid();
        MonkComboComponent state = getOrCreate(playerId);

        try {
            MonkSkillBarHud hud = new MonkSkillBarHud(plugin, ctx.playerRef());
            huds.put(playerId, hud);
            ctx.player().getHudManager().setCustomHud(ctx.playerRef(), hud);
            hud.show();
            hud.scheduleInitialRefresh(state);
        } catch (Exception e) {
            huds.remove(playerId);
            plugin.getLogger().at(Level.WARNING).withCause(e).log(
                "MonkAsura: falha ao anexar HUD para %s",
                ctx.playerRef().getUsername()
            );
            MonkVfxUtil.notifyWarning(
                ctx.playerRef(),
                "Barra de skills indisponível. Use teclas 1/2/3 e /monkskills."
            );
        }
    }

    public void refreshHud(@Nonnull UUID playerId) {
        if (!isHudEnabled(playerId)) {
            return;
        }
        MonkSkillBarHud hud = huds.get(playerId);
        if (hud != null) {
            hud.refresh(getOrCreate(playerId));
        }
    }

    public void refreshFuryExpiry(@Nonnull MonkComboComponent state) {
        if (state.getPhase() == MonkComboPhase.FURY_ACTIVE && !state.isFuryActive()) {
            state.setPhase(MonkComboPhase.NORMAL);
            refreshHud(state.getPlayerId());
        }
    }

    public boolean tryInvokeOrb(@Nonnull PlayerRef playerRef) {
        return runOnWorld(playerRef, () -> {
            PlayerContext ctx = PlayerContext.from(playerRef);
            if (ctx != null) {
                invokeOrb(ctx);
            }
        });
    }

    public boolean tryFury(@Nonnull PlayerRef playerRef) {
        return runOnWorld(playerRef, () -> {
            PlayerContext ctx = PlayerContext.from(playerRef);
            if (ctx != null) {
                activateFury(ctx);
            }
        });
    }

    public boolean tryAsura(@Nonnull PlayerRef playerRef) {
        return runOnWorld(playerRef, () -> {
            PlayerContext ctx = PlayerContext.from(playerRef);
            if (ctx != null) {
                startAsura(ctx);
            }
        });
    }

    private boolean runOnWorld(@Nonnull PlayerRef playerRef, @Nonnull Runnable action) {
        if (!playerRef.isValid()) {
            return false;
        }
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world == null) {
            return false;
        }
        world.execute(action);
        return true;
    }

    private void invokeOrb(@Nonnull PlayerContext ctx) {
        MonkComboComponent state = getOrCreate(ctx.playerRef().getUuid());
        refreshFuryExpiry(state);

        if (state.getPhase() == MonkComboPhase.ASURA_CHARGING) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Concentração em andamento…");
            return;
        }

        MonkAsuraConfig config = plugin.getConfig();
        if (state.getOrbCount() >= config.getMaxOrbs()) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Você já possui o máximo de esferas (" + config.getMaxOrbs() + ").");
            refreshHud(state.getPlayerId());
            return;
        }

        state.setOrbCount(state.getOrbCount() + 1);
        int orbIndex = state.getOrbCount();
        MonkVfxUtil.spawnOrbCreation(ctx, orbIndex, config.getMaxOrbs(), config);
        plugin.getOrbVisualSystem().burstOrbCreated(ctx, orbIndex);
        syncOrbAuraEffect(ctx, state);
        refreshHud(state.getPlayerId());
        MonkVfxUtil.notifySkill(
            ctx.playerRef(),
            "Invocar Esfera — orbes " + state.getOrbCount() + "/" + config.getMaxOrbs()
        );
    }

    private void activateFury(@Nonnull PlayerContext ctx) {
        MonkComboComponent state = getOrCreate(ctx.playerRef().getUuid());
        refreshFuryExpiry(state);

        if (state.getPhase() == MonkComboPhase.ASURA_CHARGING) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Concentração em andamento…");
            return;
        }

        MonkAsuraConfig config = plugin.getConfig();
        if (state.getOrbCount() < config.getMaxOrbs()) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Precisa de 5 esferas ativas.");
            refreshHud(state.getPlayerId());
            return;
        }

        state.setOrbCount(0);
        state.setPhase(MonkComboPhase.FURY_ACTIVE);
        state.setFuryUsedSinceLastAsura(true);
        state.setFuryEndTimeMs(System.currentTimeMillis() + (long) (config.getFuryDurationSeconds() * 1000L));

        removeOrbAuraEffect(ctx);
        applyFuryEffect(ctx, config.getFuryDurationSeconds());
        MonkVfxUtil.playFuryLoop(ctx);
        MonkVfxUtil.showFuryTitle(ctx.playerRef());
        MonkVfxUtil.notifySkill(ctx.playerRef(), "Fúria ativada!");
        refreshHud(state.getPlayerId());

        HytaleServer.SCHEDULED_EXECUTOR.schedule(
            () -> ctx.world().execute(() -> onFuryExpired(ctx.playerRef().getUuid())),
            (long) config.getFuryDurationSeconds(),
            TimeUnit.SECONDS
        );
    }

    private void applyFuryEffect(@Nonnull PlayerContext ctx, float durationSeconds) {
        EffectControllerComponent controller = getEffectController(ctx);
        if (controller == null) {
            return;
        }
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(FURY_EFFECT_ID);
        if (effect == null) {
            return;
        }
        int index = EntityEffect.getAssetMap().getIndex(FURY_EFFECT_ID);
        controller.addEffect(
            ctx.entityRef(),
            index,
            effect,
            durationSeconds,
            OverlapBehavior.OVERWRITE,
            ctx.store()
        );
    }

    private void syncOrbAuraEffect(@Nonnull PlayerContext ctx, @Nonnull MonkComboComponent state) {
        EffectControllerComponent controller = getEffectController(ctx);
        if (controller == null) {
            return;
        }
        int index = EntityEffect.getAssetMap().getIndex(ORB_AURA_EFFECT_ID);
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(ORB_AURA_EFFECT_ID);
        if (effect == null || index < 0) {
            return;
        }

        if (state.getOrbCount() > 0 && state.getPhase() != MonkComboPhase.ASURA_CHARGING) {
            if (!controller.hasEffect(index)) {
                controller.addInfiniteEffect(ctx.entityRef(), index, effect, ctx.store());
            }
        } else {
            removeOrbAuraEffect(ctx);
        }
    }

    private void removeOrbAuraEffect(@Nonnull PlayerContext ctx) {
        EffectControllerComponent controller = getEffectController(ctx);
        if (controller == null) {
            return;
        }
        int index = EntityEffect.getAssetMap().getIndex(ORB_AURA_EFFECT_ID);
        if (index >= 0 && controller.hasEffect(index)) {
            controller.removeEffect(ctx.entityRef(), index, ctx.store());
        }
    }

    @javax.annotation.Nullable
    private EffectControllerComponent getEffectController(@Nonnull PlayerContext ctx) {
        return ctx.store().getComponent(ctx.entityRef(), EffectControllerComponent.getComponentType());
    }

    private void onFuryExpired(@Nonnull UUID playerId) {
        MonkComboComponent state = states.get(playerId);
        if (state == null) {
            return;
        }
        if (state.getPhase() == MonkComboPhase.FURY_ACTIVE) {
            state.setPhase(MonkComboPhase.NORMAL);
        }
        refreshHud(playerId);
    }

    private void startAsura(@Nonnull PlayerContext ctx) {
        MonkComboComponent state = getOrCreate(ctx.playerRef().getUuid());
        refreshFuryExpiry(state);

        if (state.getPhase() == MonkComboPhase.ASURA_CHARGING) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Concentração em andamento…");
            return;
        }

        if (!state.isFuryUsedSinceLastAsura()) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Ative Fúria antes do Punho de Asura.");
            refreshHud(state.getPlayerId());
            return;
        }

        MonkAsuraConfig config = plugin.getConfig();
        if (state.getOrbCount() < config.getMaxOrbs()) {
            MonkVfxUtil.notifyWarning(ctx.playerRef(), "Reúna 5 esferas após a Fúria.");
            refreshHud(state.getPlayerId());
            return;
        }

        state.setPhase(MonkComboPhase.ASURA_CHARGING);
        state.setAsuraConvergeProgress(0f);
        refreshHud(state.getPlayerId());

        MonkVfxUtil.spawnOnPlayer(ctx, MonkVfxUtil.PARTICLE_ASURA_CHARGE);
        MonkVfxUtil.notifySkill(ctx.playerRef(), "Punho Supremo de Asura — concentrando…");

        long chargeMs = (long) (config.getAsuraChargeSeconds() * 1000L);
        int steps = Math.max(1, (int) (chargeMs / 100L));
        long stepMs = chargeMs / steps;

        for (int step = 1; step <= steps; step++) {
            final float progress = step / (float) steps;
            final int s = step;
            HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> ctx.world().execute(() -> {
                    if (s < steps) {
                        state.setAsuraConvergeProgress(progress);
                    } else {
                        executeAsuraStrike(ctx, state);
                    }
                }),
                stepMs * s,
                TimeUnit.MILLISECONDS
            );
        }
    }

    private void executeAsuraStrike(@Nonnull PlayerContext ctx, @Nonnull MonkComboComponent state) {
        MonkAsuraConfig config = plugin.getConfig();
        state.setOrbCount(0);
        state.setAsuraConvergeProgress(1f);
        removeOrbAuraEffect(ctx);

        Vector3d center = ctx.playerRef().getTransform().getPosition();
        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ASURA_IMPACT, center);
        MonkVfxUtil.spawnAt(ctx, MonkVfxUtil.PARTICLE_ASURA_EXPLOSION, center);
        MonkVfxUtil.showAsuraTitle(ctx.playerRef());
        MonkVfxUtil.notifySkill(ctx.playerRef(), "Punho Supremo de Asura!");

        List<Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore>> targets =
            findNearbyHostiles(ctx, config.getAsuraRadius());

        Damage.EntitySource source = new Damage.EntitySource(ctx.entityRef());
        int causeIndex = DamageCause.getAssetMap().getIndex(DamageCause.PHYSICAL.getId());

        for (Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> target : targets) {
            float damage = computeDamage(ctx.store(), target, config);
            Damage damageEvent = new Damage(source, causeIndex, damage);
            DamageSystems.executeDamage(target, ctx.store(), damageEvent);
        }

        state.setPhase(MonkComboPhase.NORMAL);
        state.setFuryUsedSinceLastAsura(false);
        state.setAsuraConvergeProgress(0f);
        refreshHud(state.getPlayerId());
    }

    private float computeDamage(
        @Nonnull Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store,
        @Nonnull Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> target,
        @Nonnull MonkAsuraConfig config
    ) {
        float damage = config.getAsuraBaseDamage();
        EntityStatMap stats = store.getComponent(target, EntityStatMap.getComponentType());
        if (stats != null) {
            EntityStatValue health = stats.get("Health");
            if (health != null) {
                float cap = health.getMax() * config.getMaxDamagePercentOfMaxHealth();
                damage = Math.min(damage, cap);
            }
        }
        return Math.max(1f, damage);
    }

    @Nonnull
    private List<Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore>> findNearbyHostiles(
        @Nonnull PlayerContext ctx,
        float radius
    ) {
        List<Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore>> result = new ArrayList<>();
        Vector3d center = ctx.playerRef().getTransform().getPosition();
        double radiusSq = radius * radius;

        ctx.store().forEachChunk((chunk, buffer) -> {
            for (int i = 0; i < chunk.size(); i++) {
                Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref = chunk.getReferenceTo(i);
                if (ref == null || !ref.isValid() || ref.equals(ctx.entityRef())) {
                    continue;
                }
                if (chunk.getComponent(i, Player.getComponentType()) != null) {
                    continue;
                }
                if (chunk.getComponent(i, NPCEntity.getComponentType()) == null) {
                    continue;
                }
                TransformComponent transform = chunk.getComponent(i, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }
                Vector3d pos = transform.getPosition();
                double dx = pos.getX() - center.getX();
                double dz = pos.getZ() - center.getZ();
                double dy = pos.getY() - center.getY();
                if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                    result.add(ref);
                }
            }
        });
        return result;
    }
}
