package com.noctisheroes.common.combat.damage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DamageManager {

    private final Set<String> immunities = new HashSet<>();
    private final Map<String, Float> resistances = new HashMap<>();
    private final Map<String, Float> weaknesses = new HashMap<>();

    private boolean ignoreFallDamage = false;

    // =============================
    // CONFIG
    // =============================

    public DamageManager immuneTo(String tag) {
        immunities.add(tag);
        return this;
    }

    public DamageManager resist(String tag, float percent) {
        resistances.put(tag, percent);
        return this;
    }

    public DamageManager weakTo(String tag, float percent) {
        weaknesses.put(tag, percent);
        return this;
    }

    public DamageManager ignoreFallDamage() {
        ignoreFallDamage = true;
        return this;
    }

    // =============================
    // LÓGICA
    // =============================

    public boolean canBeDamaged(DamageConfig ctx) {
        for (String tag : immunities) {
            if (ctx.hasTag(tag)) return false;
        }
        return true;
    }

    public float applyModifiers(DamageConfig ctx, float damage) {

        for (var entry : resistances.entrySet()) {
            if (ctx.hasTag(entry.getKey())) {
                damage *= (1.0f - entry.getValue());
            }
        }

        for (var entry : weaknesses.entrySet()) {
            if (ctx.hasTag(entry.getKey())) {
                damage *= (1.0f + entry.getValue());
            }
        }

        return damage;
    }

    public boolean shouldTakeFallDamage() {
        return !ignoreFallDamage;
    }
}