package com.noctisheroes.entity.entities.drakari;

import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.combat.rage.RageConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.attribute.AttributeManager;
import com.noctisheroes.entity.components.RageComponent;
import com.noctisheroes.entity.entities.base.AbstractDrakari;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class DrakariSoldier extends AbstractDrakari {

    private static final AttributeConfig ATTRIBUTES =
            new AttributeConfig.Builder()
                    .maxHealth(150.0)
                    .movementSpeed(0.35)
                    .flyingSpeed(0.6)
                    .attackDamage(12.0)
                    .attackKnockback(0.75)
                    .attackSpeed(0.4)
                    .followRange(50.0)
                    .armor(0.3)
                    .armorToughness(0.3)
                    .knockbackResistance(0.5)
                    .explosionResistance(0.3f)
                    .projectileResistance(0.3f)
                    .build();

    private static final EntityConfig CONFIG =
            new EntityConfig.Builder()
                    .xpReward(50)
                    .skinCount(2)
                    .build();

    private static final RageConfig RAGE =
            new RageConfig.Builder()
                    .max(100)
                    .passiveGain(0.01f)
                    .damageMultiplier(0.8f)
                    .build();

    public DrakariSoldier(EntityType<? extends Monster> type, Level level) {
        super(type, level, "drakari", ATTRIBUTES, CONFIG);
        this.initRage(RAGE);
    }

    public static AttributeSupplier createAttributes() {
        return new AttributeManager(ATTRIBUTES).build();
    }

}