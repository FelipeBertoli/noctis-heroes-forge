package com.noctisheroes.common.managers;

import com.noctisheroes.common.config.AttributeConfig;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

public class AttributeManager {

    private final AttributeConfig config;

    public AttributeManager(AttributeConfig config) {
        this.config = config;
    }

    public AttributeSupplier build() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, config.maxHealth)
                .add(Attributes.MOVEMENT_SPEED, config.movementSpeed)
                .add(Attributes.FLYING_SPEED, config.flyingSpeed)
                .add(Attributes.ATTACK_DAMAGE, config.attackDamage)
                .add(Attributes.ATTACK_KNOCKBACK, config.attackKnockback)
                .add(Attributes.ATTACK_SPEED, config.attackSpeed)
                .add(Attributes.FOLLOW_RANGE, config.followRange)
                .add(Attributes.ARMOR, config.armor)
                .add(Attributes.ARMOR_TOUGHNESS, config.armorToughness)
                .add(Attributes.KNOCKBACK_RESISTANCE, config.knockbackResistance)
                .build();
    }
}