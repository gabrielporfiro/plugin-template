package com.monk.asura.commands;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.monk.asura.MonkAsuraPlugin;

import javax.annotation.Nonnull;

public class MonkAsuraCommand extends AbstractMonkSkillCommand {

    public MonkAsuraCommand(@Nonnull MonkAsuraPlugin plugin) {
        super(plugin, "monkasura", "Teste: Punho Supremo de Asura", "asura");
    }

    @Override
    protected boolean invokeSkill(@Nonnull PlayerRef playerRef) {
        return MonkAsuraPlugin.getInstance().getComboService().tryAsura(playerRef);
    }

    @Override
    @Nonnull
    protected String successMessage() {
        return "Comando: Punho de Asura iniciado.";
    }
}
