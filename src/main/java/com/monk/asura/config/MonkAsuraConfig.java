package com.monk.asura.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class MonkAsuraConfig {

    @Nonnull
    public static final BuilderCodec<MonkAsuraConfig> CODEC = BuilderCodec
        .builder(MonkAsuraConfig.class, MonkAsuraConfig::new)
        .append(new KeyedCodec<>("MaxOrbs", Codec.INTEGER), (c, v) -> c.maxOrbs = v, c -> c.maxOrbs).add()
        .append(new KeyedCodec<>("FuryDurationSeconds", Codec.FLOAT), (c, v) -> c.furyDurationSeconds = v, c -> c.furyDurationSeconds).add()
        .append(new KeyedCodec<>("AsuraChargeSeconds", Codec.FLOAT), (c, v) -> c.asuraChargeSeconds = v, c -> c.asuraChargeSeconds).add()
        .append(new KeyedCodec<>("AsuraBaseDamage", Codec.FLOAT), (c, v) -> c.asuraBaseDamage = v, c -> c.asuraBaseDamage).add()
        .append(new KeyedCodec<>("AsuraRadius", Codec.FLOAT), (c, v) -> c.asuraRadius = v, c -> c.asuraRadius).add()
        .append(new KeyedCodec<>("OrbitRadius", Codec.FLOAT), (c, v) -> c.orbitRadius = v, c -> c.orbitRadius).add()
        .append(new KeyedCodec<>("MaxDamagePercentOfMaxHealth", Codec.FLOAT), (c, v) -> c.maxDamagePercentOfMaxHealth = v, c -> c.maxDamagePercentOfMaxHealth).add()
        .append(new KeyedCodec<>("OrbitSpeed", Codec.FLOAT), (c, v) -> c.orbitSpeed = v, c -> c.orbitSpeed).add()
        .build();

    private int maxOrbs = 5;
    private float furyDurationSeconds = 30f;
    private float asuraChargeSeconds = 2f;
    private float asuraBaseDamage = 1500f;
    private float asuraRadius = 5f;
    private float orbitRadius = 1.75f;
    private float maxDamagePercentOfMaxHealth = 0.4f;
    private float orbitSpeed = 1.8f;

    public int getMaxOrbs() {
        return maxOrbs;
    }

    public float getFuryDurationSeconds() {
        return furyDurationSeconds;
    }

    public float getAsuraChargeSeconds() {
        return asuraChargeSeconds;
    }

    public float getAsuraBaseDamage() {
        return asuraBaseDamage;
    }

    public float getAsuraRadius() {
        return asuraRadius;
    }

    public float getOrbitRadius() {
        return orbitRadius;
    }

    public float getMaxDamagePercentOfMaxHealth() {
        return maxDamagePercentOfMaxHealth;
    }

    public float getOrbitSpeed() {
        return orbitSpeed;
    }
}
