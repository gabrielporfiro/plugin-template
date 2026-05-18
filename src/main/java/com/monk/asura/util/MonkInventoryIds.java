package com.monk.asura.util;

import com.hypixel.hytale.server.core.inventory.Inventory;

/**
 * Slots da hotbar usados pelas skills (teclas 4, 5 e 6 — não conflitam com 1–3).
 */
public final class MonkInventoryIds {

    @SuppressWarnings("removal")
    public static final int HOTBAR_SECTION = Inventory.HOTBAR_SECTION_ID;

    /** Tecla 4 */
    public static final int SLOT_ORB = 3;
    /** Tecla 5 */
    public static final int SLOT_FURY = 4;
    /** Tecla 6 */
    public static final int SLOT_ASURA = 5;

    private MonkInventoryIds() {
    }
}
