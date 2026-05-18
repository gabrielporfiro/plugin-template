package com.monk.asura.commands;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.monk.asura.MonkAsuraPlugin;

import javax.annotation.Nonnull;

public class MonkFuryCommand extends AbstractMonkSkillCommand {

    public MonkFuryCommand(@Nonnull MonkAsuraPlugin plugin) {
        super(plugin, "monkfuria", "Teste: ativa Fúria (precisa de 5 orbes)", "fury");
    }

    @Override
    protected boolean invokeSkill(@Nonnull PlayerRef playerRef) {
        return MonkAsuraPlugin.getInstance().getComboService().tryFury(playerRef);
    }

    @Override
    @Nonnull
    protected String successMessage() {
        return "Comando: Fúria executado.";
    }
}
