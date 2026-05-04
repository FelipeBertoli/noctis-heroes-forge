package com.noctisheroes.entity.entities.viltrumite.mob;

import com.noctisheroes.common.config.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.managers.AttributeManager;
import com.noctisheroes.entity.entities.viltrumite.base.ViltrumiteBoss;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;


public class ThraggBoss extends ViltrumiteBoss {

    private static final AttributeConfig ATTRIBUTES = new AttributeConfig.Builder()
            .maxHealth(500.0)
            .movementSpeed(0.4)
            .flyingSpeed(0.8)
            .attackDamage(20.0)
            .attackKnockback(1.0)
            .attackSpeed(0.5)
            .followRange(50.0)
            .armor(0.9)
            .armorToughness(0.36)
            .knockbackResistance(1.0)
            .explosionResistance(1.0f)
            .fireResistance(1.0f)
            .projectileResistance(1.0f)
            .build();

    private static final EntityConfig CONFIG =
            new EntityConfig.Builder()
                    .xpReward(100)
                    .skinCount(1)
                    .build();

        public ThraggBoss(EntityType<? extends Monster> type, Level level) {
            super(type, level, "thragg", ATTRIBUTES, CONFIG);
            this.getBossComponent();
        }

    public static AttributeSupplier createAttributes() {
        return new AttributeManager(ATTRIBUTES).build();
    }

}