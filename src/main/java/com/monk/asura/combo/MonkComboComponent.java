package com.monk.asura.combo;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Estado do combo Monk por jogador (fonte da verdade em memória).
 */
public class MonkComboComponent {

    private final UUID playerId;
    private int orbCount;
    private MonkComboPhase phase = MonkComboPhase.NORMAL;
    private boolean furyUsedSinceLastAsura;
    private long furyEndTimeMs;
    private float asuraConvergeProgress;
    private double orbitAngle;

    public MonkComboComponent(@Nonnull UUID playerId) {
        this.playerId = playerId;
    }

    @Nonnull
    public UUID getPlayerId() {
        return playerId;
    }

    public int getOrbCount() {
        return orbCount;
    }

    public void setOrbCount(int orbCount) {
        this.orbCount = orbCount;
    }

    @Nonnull
    public MonkComboPhase getPhase() {
        return phase;
    }

    public void setPhase(@Nonnull MonkComboPhase phase) {
        this.phase = phase;
    }

    public boolean isFuryUsedSinceLastAsura() {
        return furyUsedSinceLastAsura;
    }

    public void setFuryUsedSinceLastAsura(boolean furyUsedSinceLastAsura) {
        this.furyUsedSinceLastAsura = furyUsedSinceLastAsura;
    }

    public long getFuryEndTimeMs() {
        return furyEndTimeMs;
    }

    public void setFuryEndTimeMs(long furyEndTimeMs) {
        this.furyEndTimeMs = furyEndTimeMs;
    }

    public float getAsuraConvergeProgress() {
        return asuraConvergeProgress;
    }

    public void setAsuraConvergeProgress(float asuraConvergeProgress) {
        this.asuraConvergeProgress = asuraConvergeProgress;
    }

    public double getOrbitAngle() {
        return orbitAngle;
    }

    public void setOrbitAngle(double orbitAngle) {
        this.orbitAngle = orbitAngle;
    }

    public boolean isFuryActive() {
        return phase == MonkComboPhase.FURY_ACTIVE && System.currentTimeMillis() < furyEndTimeMs;
    }

    public void reset() {
        orbCount = 0;
        phase = MonkComboPhase.NORMAL;
        furyUsedSinceLastAsura = false;
        furyEndTimeMs = 0L;
        asuraConvergeProgress = 0f;
        orbitAngle = 0d;
    }
}
