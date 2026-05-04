package com.noctisheroes.entity.entities.viltrumite.mob;

import com.noctisheroes.common.config.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.managers.AttributeManager;
import com.noctisheroes.entity.entities.viltrumite.base.AbstractViltrumite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Viltrumita comum.
 * Implementação simplificada usando padrão ViltrumiteVariant.
 */
public class ViltrumiteEntity extends AbstractViltrumite  {

    private static final AttributeConfig ATTRIBUTES =
            new AttributeConfig.Builder()
                    .maxHealth(125.0)
                    .movementSpeed(0.35)
                    .flyingSpeed(0.6)
                    .attackDamage(10.0)
                    .attackKnockback(0.75)
                    .attackSpeed(0.4)
                    .followRange(50.0)
                    .armor(0.2)
                    .armorToughness(0.2)
                    .knockbackResistance(0.5)
                    .explosionResistance(0.3f)
                    .fireResistance(0.3f)
                    .projectileResistance(0.3f)
                    .build();

    private static final EntityConfig CONFIG =
            new EntityConfig.Builder()
                    .xpReward(50)
                    .skinCount(6)
                    .build();

    public ViltrumiteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "viltrumite", ATTRIBUTES, CONFIG);
    }

    public static AttributeSupplier createAttributes() {
        return new AttributeManager(ATTRIBUTES).build();
    }
}