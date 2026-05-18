package com.monk.asura.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.combo.MonkComboComponent;
import com.monk.asura.config.MonkAsuraConfig;
import com.monk.asura.skills.SkillDefinition;

import javax.annotation.Nonnull;

public class MonkSkillsMenuPage extends CustomUIPage {

    private final MonkAsuraPlugin plugin;

    public MonkSkillsMenuPage(@Nonnull PlayerRef playerRef, @Nonnull MonkAsuraPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.plugin = plugin;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder uiCommandBuilder,
        @Nonnull UIEventBuilder uiEventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        uiCommandBuilder.append("monk/skills_menu.ui");
        applyTooltips(uiCommandBuilder);
    }

    private void applyTooltips(@Nonnull UICommandBuilder builder) {
        MonkComboComponent state = plugin.getComboService().getOrCreate(playerRef.getUuid());
        MonkAsuraConfig config = plugin.getConfig();
        int max = config.getMaxOrbs();

        builder.set("#SkillSlot1.TooltipText", SkillDefinition.INVOKE_ORB.buildTooltip(state, max));
        builder.set("#SkillSlot2.TooltipText", SkillDefinition.FURY.buildTooltip(state, max));
        builder.set("#SkillSlot3.TooltipText", SkillDefinition.ASURA.buildTooltip(state, max));
    }
}
