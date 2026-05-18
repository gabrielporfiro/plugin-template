package com.monk.asura.ui;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.combo.MonkComboComponent;
import com.monk.asura.combo.MonkComboPhase;
import com.monk.asura.config.MonkAsuraConfig;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class MonkSkillBarHud extends CustomUIHud {

    private static final String PATCH_ORB = "PatchStyle(TexturePath: \"skill_orb.png\")";
    private static final String PATCH_ORB_DIM = "PatchStyle(TexturePath: \"skill_bg.png\")";
    private static final String PATCH_FURY = "PatchStyle(TexturePath: \"skill_fury.png\")";
    private static final String PATCH_FURY_DIM = "PatchStyle(TexturePath: \"skill_bg.png\")";
    private static final String PATCH_ASURA = "PatchStyle(TexturePath: \"skill_asura.png\")";
    private static final String PATCH_ASURA_DIM = "PatchStyle(TexturePath: \"skill_bg.png\")";

    private final MonkAsuraPlugin plugin;
    private boolean skill1Ready;
    private boolean skill2Ready;
    private boolean skill3Unlocked;
    private boolean skill3Ready;

    public MonkSkillBarHud(@Nonnull MonkAsuraPlugin plugin, @Nonnull PlayerRef playerRef) {
        super(playerRef);
        this.plugin = plugin;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        builder.append("monk/skill_bar.ui");
    }

    public void scheduleInitialRefresh(@Nonnull MonkComboComponent state) {
        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            PlayerRef playerRef = getPlayerRef();
            if (!playerRef.isValid()) {
                return;
            }
            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) {
                return;
            }
            world.execute(() -> refresh(state));
        }, 200, TimeUnit.MILLISECONDS);
    }

    public void refresh(@Nonnull MonkComboComponent state) {
        MonkAsuraConfig config = plugin.getConfig();
        int max = config.getMaxOrbs();

        skill1Ready = state.getOrbCount() < max;
        skill2Ready = state.getOrbCount() >= max;
        skill3Unlocked = state.isFuryUsedSinceLastAsura()
            || state.isFuryActive()
            || state.getPhase() == MonkComboPhase.ASURA_CHARGING;
        skill3Ready = skill3Unlocked && state.getOrbCount() >= max;

        UICommandBuilder builder = new UICommandBuilder();
        applyState(builder);
        update(false, builder);
    }

    private void applyState(@Nonnull UICommandBuilder builder) {
        builder.set("#Skill1.Background", skill1Ready ? PATCH_ORB : PATCH_ORB_DIM);
        builder.set("#Skill2.Background", skill2Ready ? PATCH_FURY : PATCH_FURY_DIM);
        builder.set("#Skill3.Background", skill3Ready ? PATCH_ASURA : PATCH_ASURA_DIM);
        builder.set("#Skill3Glow.Visible", skill3Unlocked);
    }
}
