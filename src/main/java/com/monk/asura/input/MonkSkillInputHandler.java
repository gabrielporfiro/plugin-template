package com.monk.asura.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Habilidade 1 / 2 / 3 do jogador (slots dedicados, sem bloquear a hotbar 1–9).
 * Vincule em Opções → Controles → Habilidade 1, 2 e 3.
 */
public class MonkSkillInputHandler implements PlayerPacketFilter {

    private final MonkAsuraPlugin plugin;
    @Nullable
    private PacketFilter inboundFilter;

    public MonkSkillInputHandler(@Nonnull MonkAsuraPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        inboundFilter = PacketAdapters.registerInbound(this);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);
    }

    public void unregister() {
        if (inboundFilter != null) {
            PacketAdapters.deregisterInbound(inboundFilter);
            inboundFilter = null;
        }
    }

    private void onPlayerInteract(@Nonnull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) {
            return;
        }
        PlayerRef universeRef = resolveUniverseRef(player, event.getPlayerRef());
        if (universeRef == null) {
            return;
        }
        if (handleAbility(universeRef, event.getActionType())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull com.hypixel.hytale.protocol.Packet packet) {
        if (!(packet instanceof SyncInteractionChains chains) || chains.updates == null) {
            return false;
        }
        boolean handled = false;
        for (SyncInteractionChain chain : chains.updates) {
            if (chain == null || chain.interactionType == null) {
                continue;
            }
            handled |= handleAbility(playerRef, chain.interactionType);
        }
        return handled;
    }

    private boolean handleAbility(@Nonnull PlayerRef playerRef, @Nonnull InteractionType type) {
        return switch (type) {
            case Ability1 -> plugin.getComboService().tryInvokeOrb(playerRef);
            case Ability2 -> plugin.getComboService().tryFury(playerRef);
            case Ability3 -> plugin.getComboService().tryAsura(playerRef);
            default -> false;
        };
    }

    @Nullable
    private PlayerRef resolveUniverseRef(
        @Nonnull Player player,
        @Nonnull Ref<EntityStore> entityRef
    ) {
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();
        return store.getComponent(entityRef, PlayerRef.getComponentType());
    }
}
