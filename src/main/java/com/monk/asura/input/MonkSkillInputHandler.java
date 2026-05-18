package com.monk.asura.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.debug.MonkDebugLog;
import com.monk.asura.util.MonkInventoryIds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Skills via teclas 4 / 5 / 6 da hotbar (padrão confiável) ou Habilidade 1/2/3 em Controles.
 * Q no cliente costuma ser "Usar" ({@link InteractionType#Use}), não Habilidade 1.
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
        InteractionType action = event.getActionType();
        // #region agent log
        MonkDebugLog.log("B", "MonkSkillInputHandler.onPlayerInteract", "PlayerInteractEvent",
            MonkDebugLog.map("actionType", action != null ? action.name() : "null", "player", universeRef.getUsername()));
        // #endregion
        if (action == null) {
            return;
        }
        if (handleAbility(universeRef, action, "interact-event")) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull com.hypixel.hytale.protocol.Packet packet) {
        if (packet instanceof SetActiveSlot slotPacket) {
            return handleHotbarSkill(playerRef, slotPacket);
        }
        if (!(packet instanceof SyncInteractionChains chains) || chains.updates == null) {
            return false;
        }
        boolean handled = false;
        for (SyncInteractionChain chain : chains.updates) {
            if (chain == null) {
                continue;
            }
            handled |= handleHotbarFromChain(playerRef, chain);
            if (chain.interactionType != null) {
                handled |= handleAbility(playerRef, chain.interactionType, "packet");
            }
        }
        return handled;
    }

    /**
     * Troca de hotbar no cliente chega como SwapFrom/SwapTo com {@code activeHotbarSlot},
     * não via {@link SetActiveSlot} no filtro de pacotes.
     */
    private boolean handleHotbarFromChain(@Nonnull PlayerRef playerRef, @Nonnull SyncInteractionChain chain) {
        if (chain.interactionType != InteractionType.SwapFrom
            && chain.interactionType != InteractionType.SwapTo) {
            return false;
        }
        // #region agent log
        MonkDebugLog.log("G", "MonkSkillInputHandler.handleHotbarFromChain", "Swap hotbar",
            MonkDebugLog.map("interactionType", chain.interactionType.name(),
                "activeHotbarSlot", chain.activeHotbarSlot,
                "key", chain.activeHotbarSlot + 1,
                "player", playerRef.getUsername(),
                "runId", "post-fix-v2"));
        // #endregion
        return handleHotbarSkillBySlot(playerRef, chain.activeHotbarSlot);
    }

    private boolean handleHotbarSkill(@Nonnull PlayerRef playerRef, @Nonnull SetActiveSlot slotPacket) {
        if (slotPacket.inventorySectionId != MonkInventoryIds.HOTBAR_SECTION) {
            return false;
        }
        // #region agent log
        MonkDebugLog.log("F", "MonkSkillInputHandler.handleHotbarSkill", "SetActiveSlot",
            MonkDebugLog.map("activeSlot", slotPacket.activeSlot, "player", playerRef.getUsername(),
                "runId", "post-fix-v2"));
        // #endregion
        return handleHotbarSkillBySlot(playerRef, slotPacket.activeSlot);
    }

    private boolean handleHotbarSkillBySlot(@Nonnull PlayerRef playerRef, int slot) {
        boolean result = switch (slot) {
            case MonkInventoryIds.SLOT_ORB -> plugin.getComboService().tryInvokeOrb(playerRef);
            case MonkInventoryIds.SLOT_FURY -> plugin.getComboService().tryFury(playerRef);
            case MonkInventoryIds.SLOT_ASURA -> plugin.getComboService().tryAsura(playerRef);
            default -> false;
        };
        if (result) {
            // #region agent log
            MonkDebugLog.log("G", "MonkSkillInputHandler.handleHotbarSkillBySlot", "skill triggered",
                MonkDebugLog.map("slot", slot, "key", slot + 1, "player", playerRef.getUsername(),
                    "runId", "post-fix-v2"));
            // #endregion
        }
        return result;
    }

    private boolean handleAbility(
        @Nonnull PlayerRef playerRef,
        @Nonnull InteractionType type,
        @Nonnull String source
    ) {
        boolean result = switch (type) {
            case Ability1 -> plugin.getComboService().tryInvokeOrb(playerRef);
            case Ability2 -> plugin.getComboService().tryFury(playerRef);
            case Ability3 -> plugin.getComboService().tryAsura(playerRef);
            default -> false;
        };
        // #region agent log
        MonkDebugLog.log("A", "MonkSkillInputHandler.handleAbility", "ability handled",
            MonkDebugLog.map("type", type.name(), "source", source, "result", result,
                "player", playerRef.getUsername(), "runId", "post-fix"));
        // #endregion
        return result;
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
