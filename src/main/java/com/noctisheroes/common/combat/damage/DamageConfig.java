package com.noctisheroes.common.combat.damage;

import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

import java.util.HashSet;
import java.util.Set;

public class DamageConfig {

    private final DamageSource source;
    private final Set<String> tags = new HashSet<>();

    public DamageConfig(DamageSource source) {
        this.source = source;

        // Mapeamento automático
        if (source.is(DamageTypeTags.IS_FIRE)) {
            tags.add(DamageTags.FIRE);
        }

        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            tags.add(DamageTags.EXPLOSION);
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            tags.add(DamageTags.PROJECTILE);
        }

        if (source.getEntity() instanceof NoctisEntity attacker) {
            tags.add(attacker.getEntityTag());
        }

        if (!isFire() && !isExplosion() && !isProjectile()) {
            tags.add(DamageTags.PHYSICAL);
        }

        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            tags.add(DamageTags.TRUE_DAMAGE);
        }

    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public DamageSource getSource() {
        return source;
    }

    public boolean isTrueDamage() {
        return hasTag(DamageTags.TRUE_DAMAGE);
    }

    public boolean isPhysical() {
        return hasTag(DamageTags.PHYSICAL);
    }

    public boolean isFire() {
        return hasTag(DamageTags.FIRE);
    }

    public boolean isExplosion() {
        return hasTag(DamageTags.EXPLOSION);
    }

    public boolean isProjectile() {
        return hasTag(DamageTags.PROJECTILE);
    }


}
