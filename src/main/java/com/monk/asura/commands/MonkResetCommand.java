package com.monk.asura.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.combo.MonkComboComponent;
import com.monk.asura.util.MonkMessages;
import com.monk.asura.util.PlayerContext;

import javax.annotation.Nonnull;
import java.util.UUID;

public class MonkResetCommand extends AbstractPlayerCommand {

    private final MonkAsuraPlugin plugin;

    public MonkResetCommand(@Nonnull MonkAsuraPlugin plugin) {
        super("monkreset", "Teste: zera orbes, fúria e estado de Asura");
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
        UUID playerId = playerRef.getUuid();
        MonkComboComponent state = plugin.getComboService().getOrCreate(playerId);
        state.reset();

        world.execute(() -> {
            PlayerContext ctx = PlayerContext.from(playerRef);
            if (ctx != null) {
                plugin.getComboService().refreshHud(playerId);
            }
        });

        context.sendMessage(MonkMessages.info("Estado Monk resetado (orbes, fúria e Asura)."));
    }
}
