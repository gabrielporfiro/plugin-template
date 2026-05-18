package com.monk.asura.input;

import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.monk.asura.MonkAsuraPlugin;
import com.monk.asura.util.MonkInventoryIds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Intercepta {@link SetActiveSlot} na hotbar (teclas 1–3) conforme guia de
 * personalização de input por pacotes do Hytale.
 */
public class MonkSkillInputHandler implements PlayerPacketFilter {

    private static final int SKILL_SLOT_ORB = 0;
    private static final int SKILL_SLOT_FURY = 1;
    private static final int SKILL_SLOT_ASURA = 2;

    private final MonkAsuraPlugin plugin;
    @Nullable
    private PacketFilter inboundFilter;

    public MonkSkillInputHandler(@Nonnull MonkAsuraPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        inboundFilter = PacketAdapters.registerInbound(this);
    }

    public void unregister() {
        if (inboundFilter != null) {
            PacketAdapters.deregisterInbound(inboundFilter);
            inboundFilter = null;
        }
    }

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull com.hypixel.hytale.protocol.Packet packet) {
        if (!(packet instanceof SetActiveSlot slotPacket)) {
            return false;
        }
        if (slotPacket.inventorySectionId != MonkInventoryIds.HOTBAR_SECTION) {
            return false;
        }
        return handleHotbarSkill(playerRef, slotPacket.activeSlot);
    }

    private boolean handleHotbarSkill(@Nonnull PlayerRef playerRef, int slot) {
        return switch (slot) {
            case SKILL_SLOT_ORB -> plugin.getComboService().tryInvokeOrb(playerRef);
            case SKILL_SLOT_FURY -> plugin.getComboService().tryFury(playerRef);
            case SKILL_SLOT_ASURA -> plugin.getComboService().tryAsura(playerRef);
            default -> false;
        };
    }
}
