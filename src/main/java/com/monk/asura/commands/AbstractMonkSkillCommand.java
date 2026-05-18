package com.monk.asura.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.debug.MonkDebugLog;
import com.monk.asura.util.MonkMessages;

import javax.annotation.Nonnull;

abstract class AbstractMonkSkillCommand extends AbstractPlayerCommand {

    private final MonkAsuraPlugin plugin;
    private final String skillId;

    AbstractMonkSkillCommand(
        @Nonnull MonkAsuraPlugin plugin,
        @Nonnull String name,
        @Nonnull String description,
        @Nonnull String skillId
    ) {
        super(name, description);
        this.plugin = plugin;
        this.skillId = skillId;
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        // #region agent log
        MonkDebugLog.log("H", "AbstractMonkSkillCommand.execute", "command",
            MonkDebugLog.map("command", getName(), "skillId", skillId,
                "player", playerRef.getUsername(), "runId", "cmd-test"));
        // #endregion

        boolean scheduled = invokeSkill(playerRef);
        if (scheduled) {
            context.sendMessage(MonkMessages.skill(successMessage()));
        } else {
            context.sendMessage(MonkMessages.warning(
                "Não foi possível executar " + skillId + " (jogador inválido ou sem mundo)."
            ));
        }
    }

    protected abstract boolean invokeSkill(@Nonnull PlayerRef playerRef);

    @Nonnull
    protected abstract String successMessage();
}
