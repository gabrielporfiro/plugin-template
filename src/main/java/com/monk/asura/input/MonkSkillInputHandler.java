package com.monk.asura.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.monk.asura.MonkAsuraPlugin;

import javax.annotation.Nonnull;

/**
 * Teclas 1, 2 e 3 da hotbar = Invocar Esfera, Fúria e Punho de Asura.
 */
public class MonkSkillInputHandler implements PlayerPacketFilter {

    private static final int SKILL_SLOT_ORB = 0;
    private static final int SKILL_SLOT_FURY = 1;
    private static final int SKILL_SLOT_ASURA = 2;

    private final MonkAsuraPlugin plugin;

    public MonkSkillInputHandler(@Nonnull MonkAsuraPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        PacketAdapters.registerInbound(this);
    }

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull com.hypixel.hytale.protocol.Packet packet) {
        if (!(packet instanceof SetActiveSlot slotPacket)) {
            return false;
        }
        if (slotPacket.inventorySectionId != Inventory.HOTBAR_SECTION_ID) {
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
