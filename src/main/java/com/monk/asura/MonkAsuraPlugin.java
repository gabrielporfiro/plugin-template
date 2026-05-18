package com.monk.asura;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.monk.asura.combo.MonkComboService;
import com.monk.asura.commands.MonkSkillsCommand;
import com.monk.asura.config.MonkAsuraConfig;
import com.monk.asura.input.MonkSkillInputHandler;
import com.monk.asura.util.PlayerContext;
import com.monk.asura.visual.MonkVfxUtil;
import com.monk.asura.visual.OrbVisualSystem;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class MonkAsuraPlugin extends JavaPlugin {

    private static MonkAsuraPlugin instance;

    private final Config<MonkAsuraConfig> config;
    private MonkComboService comboService;
    private OrbVisualSystem orbVisualSystem;
    private MonkSkillInputHandler inputHandler;

    public MonkAsuraPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = withConfig(MonkAsuraConfig.CODEC);
    }

    public static MonkAsuraPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;
        comboService = new MonkComboService(this);
        orbVisualSystem = new OrbVisualSystem(this);
        inputHandler = new MonkSkillInputHandler(this);

        getCommandRegistry().registerCommand(new MonkSkillsCommand(this));
        inputHandler.register();

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        getLogger().at(Level.INFO).log("MonkAsura: sistema de habilidades registrado.");
    }

    @Override
    protected void start() {
        orbVisualSystem.start();
        getLogger().at(Level.INFO).log(
            "MonkAsura v%s ativo — F1=Esfera | F2=Fúria | F3=Asura (vincule Habilidade 1/2/3)",
            getManifest().getVersion()
        );
    }

    @Override
    protected void shutdown() {
        orbVisualSystem.stop();
        comboService.clearAllHuds();
        instance = null;
        getLogger().at(Level.INFO).log("MonkAsura desligado.");
    }

    @Nonnull
    public MonkAsuraConfig getConfig() {
        return config.get();
    }

    @Nonnull
    public MonkComboService getComboService() {
        return comboService;
    }

    @Nonnull
    public OrbVisualSystem getOrbVisualSystem() {
        return orbVisualSystem;
    }

    private void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        com.hypixel.hytale.server.core.universe.PlayerRef universeRef =
            event.getPlayer().getWorld().getEntityStore().getStore()
                .getComponent(event.getPlayerRef(), com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        PlayerContext ctx = universeRef != null ? PlayerContext.from(universeRef) : null;
        if (ctx == null) {
            return;
        }
        comboService.getOrCreate(ctx.playerRef().getUuid());
        comboService.attachHud(ctx);
        MonkVfxUtil.notifyInfo(
            ctx.playerRef(),
            "Monk: F1=Esfera | F2=Fúria | F3=Asura — vincule Habilidade 1/2/3 às teclas F1-F3"
        );
    }

    private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        comboService.detachHud(event.getPlayerRef());
        comboService.removePlayer(event.getPlayerRef().getUuid());
    }
}
