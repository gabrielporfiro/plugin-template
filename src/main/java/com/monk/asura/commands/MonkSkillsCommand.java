package com.monk.asura.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.ui.MonkSkillsMenuPage;
import com.monk.asura.util.MonkMessages;

import javax.annotation.Nonnull;

public class MonkSkillsCommand extends AbstractPlayerCommand {

    private final MonkAsuraPlugin plugin;

    public MonkSkillsCommand(@Nonnull MonkAsuraPlugin plugin) {
        super("monkskills", "Abre o menu de habilidades Monk com tooltips");
        this.plugin = plugin;
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            context.sendMessage(MonkMessages.warning("Jogador não encontrado."));
            return;
        }
        MonkSkillsMenuPage page = new MonkSkillsMenuPage(playerRef, plugin);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
