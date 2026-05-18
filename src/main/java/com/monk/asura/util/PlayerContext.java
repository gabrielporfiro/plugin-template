package com.monk.asura.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record PlayerContext(
    @Nonnull PlayerRef playerRef,
    @Nonnull Ref<EntityStore> entityRef,
    @Nonnull Store<EntityStore> store,
    @Nonnull World world,
    @Nonnull Player player
) {

    @Nullable
    public static PlayerContext from(@Nonnull PlayerRef playerRef) {
        if (!playerRef.isValid()) {
            return null;
        }
        Ref<EntityStore> entityRef = playerRef.getReference();
        if (entityRef == null || !entityRef.isValid()) {
            return null;
        }
        World world = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(playerRef.getWorldUuid());
        if (world == null) {
            return null;
        }
        Store<EntityStore> store = world.getEntityStore().getStore();
        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) {
            return null;
        }
        return new PlayerContext(playerRef, entityRef, store, world, player);
    }
}
