package com.monk.asura.commands;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.monk.asura.MonkAsuraPlugin;

import javax.annotation.Nonnull;

public class MonkOrbCommand extends AbstractMonkSkillCommand {

    public MonkOrbCommand(@Nonnull MonkAsuraPlugin plugin) {
        super(plugin, "monkorbe", "Teste: invoca 1 esfera espiritual", "orb");
    }

    @Override
    protected boolean invokeSkill(@Nonnull PlayerRef playerRef) {
        return MonkAsuraPlugin.getInstance().getComboService().tryInvokeOrb(playerRef);
    }

    @Override
    @Nonnull
    protected String successMessage() {
        return "Comando: Invocar Esfera executado.";
    }
}
