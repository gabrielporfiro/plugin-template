package com.monk.asura.visual;

import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.util.MonkTasks;
import com.monk.asura.util.PlayerContext;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * Animação de uma mão à frente (cast da varinha — CastLeft).
 */
public final class MonkAnimationUtil {

    private static final String WAND_ANIMATIONS_ID = "Wand";
    private static final String CAST_LEFT_ANIM = "CastLeft";
    private static final float CAST_DURATION_SEC = 0.7f;

    private MonkAnimationUtil() {
    }

    public static void playOrbCastAnimation(@Nonnull PlayerContext ctx) {
        ItemPlayerAnimations wand = ItemPlayerAnimations.getAssetMap().getAsset(WAND_ANIMATIONS_ID);
        if (wand != null) {
            AnimationUtils.playAnimation(
                ctx.entityRef(),
                AnimationSlot.Action,
                wand,
                CAST_LEFT_ANIM,
                ctx.store()
            );
        } else {
            AnimationUtils.playAnimation(
                ctx.entityRef(),
                AnimationSlot.Action,
                CAST_LEFT_ANIM,
                false,
                ctx.store()
            );
        }

        PlayerRef playerRef = ctx.playerRef();
        MonkAsuraPlugin plugin = MonkAsuraPlugin.getInstance();
        if (plugin == null) {
            return;
        }
        MonkTasks.schedule(
            plugin,
            () -> {
                if (!playerRef.isValid()) {
                    return;
                }
                ctx.world().execute(() ->
                    AnimationUtils.stopAnimation(ctx.entityRef(), AnimationSlot.Action, ctx.store())
                );
            },
            (long) (CAST_DURATION_SEC * 1000L),
            TimeUnit.MILLISECONDS
        );
    }
}
