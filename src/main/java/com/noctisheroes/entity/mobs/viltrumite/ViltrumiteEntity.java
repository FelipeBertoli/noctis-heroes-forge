package com.noctisheroes.entity.mobs.viltrumite;

import com.noctisheroes.entity.base.type.viltrumite.ViltrumiteVariant;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/**
 * Viltrumita comum.
 * Implementação simplificada usando padrão ViltrumiteVariant.
 */
public class ViltrumiteEntity extends ViltrumiteVariant {

    private static final ViltrumiteConfig CONFIG = new ViltrumiteConfig.Builder()
            .xpReward(30)
            .skinCount(6)
            .maxHealth(200.0)
            .movementSpeed(0.3)
            .flyingSpeed(0.6)
            .attackDamage(10.0)
            .attackKnockback(0.7)
            .attackSpeed(0.3)
            .followRange(30.0)
            .armor(0.3)
            .armorToughness(0.2)
            .knockbackResistance(0.25)
            .build();

    public ViltrumiteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "viltrumite", CONFIG);
    }

    public static AttributeSupplier setAttributes() {
        return createAttributeSupplier(CONFIG);
    }
}