package com.monk.asura.visual;

import com.hypixel.hytale.math.vector.Vector3d;

import javax.annotation.Nonnull;

/**
 * Posições de órbita em círculo horizontal: distância fixa do centro do jogador.
 */
public final class OrbOrbitUtil {

    /** Altura do centro de cada orbe em relação aos pés do jogador. */
    public static final double ORB_HEIGHT_OFFSET = 1.2;

    private OrbOrbitUtil() {
    }

    @Nonnull
    public static Vector3d computeOrbPosition(
        @Nonnull Vector3d playerCenter,
        double orbitRadius,
        double orbitAngleRad,
        int orbIndex,
        int maxOrbs
    ) {
        double slotAngle = orbitAngleRad + (Math.PI * 2.0 * orbIndex / maxOrbs);
        return new Vector3d(
            playerCenter.getX() + Math.cos(slotAngle) * orbitRadius,
            playerCenter.getY() + ORB_HEIGHT_OFFSET,
            playerCenter.getZ() + Math.sin(slotAngle) * orbitRadius
        );
    }
}
