package com.noctisheroes.entity.entities.base;

import com.noctisheroes.common.ability.abilities.DestructiveDashAbility;
import com.noctisheroes.common.ability.abilities.SuperPunchAbility;
import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.combat.damage.DamageTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
public abstract class AbstractViltrumite extends AbstractFlightWarrior {


    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected AbstractViltrumite(
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
                .resist(DamageTags.EXPLOSION, 0.5f)
                .weakTo(DamageTags.SONIC, 1.0f)
                .immuneTo("viltrumite");
    }

}