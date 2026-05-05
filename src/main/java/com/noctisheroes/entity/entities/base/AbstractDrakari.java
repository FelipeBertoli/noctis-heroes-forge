package com.noctisheroes.entity.entities.base;

import com.noctisheroes.common.ability.abilities.DestructiveDashAbility;
import com.noctisheroes.common.ability.abilities.SuperPunchAbility;
import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.combat.damage.DamageTags;
import com.noctisheroes.common.effect.EffectConfig;
import com.noctisheroes.common.effect.EffectType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class AbstractDrakari extends AbstractFlightWarrior {

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected AbstractDrakari(
            EntityType<? extends Monster> type,
            Level level,
            String tag,
            AttributeConfig attributes,
            EntityConfig config) {

        super(type, level, tag, attributes, config);

        this.getAbilityManager().register(new DestructiveDashAbility());
        this.getAbilityManager().register(new SuperPunchAbility());

        this.getDamageProfile()
                .ignoreFallDamage()
                .resist(DamageTags.FIRE, attributes.fireResistance)
                .resist(DamageTags.EXPLOSION, attributes.explosionResistance)
                .resist(DamageTags.PROJECTILE, attributes.explosionWeakness)
                .weakTo(DamageTags.SONIC, 0.75f)
                .immuneTo("drakari");

        this.getEffectManager().addEffect(
                new EffectConfig.Builder()
                        .type(EffectType.REGEN)
                        .duration(Integer.MAX_VALUE)
                        .amplifier(1)
                        .tickInterval(20)
                        .build()
        );

    }




}