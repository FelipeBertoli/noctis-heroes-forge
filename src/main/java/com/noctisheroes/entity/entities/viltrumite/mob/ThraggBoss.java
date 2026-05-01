package com.noctisheroes.entity.entities.viltrumite.mob;

import com.noctisheroes.entity.entities.viltrumite.base.ViltrumiteBoss;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/**
 * Boss Thragg - Viltrumita supremo.
 * O boss mais poderoso da série.
 */
public class ThraggBoss extends ViltrumiteBoss {

    private static final ViltrumiteConfig CONFIG = new ViltrumiteConfig.Builder()
            .xpReward(100)
            .skinCount(1)
            .maxHealth(500.0)
            .movementSpeed(0.4)
            .flyingSpeed(0.8)
            .attackDamage(20.0)
            .attackKnockback(1.0)
            .attackSpeed(0.5)
            .followRange(50.0)
            .armor(0.9)
            .armorToughness(0.36)
            .knockbackResistance(1.0)  // Imune a knockback
            .randomFlightToggleChance(0.0005f) // Quase nunca muda aleatoriamente
            .combatFlightChance(0.025f)        // Sempre voando em combate
            .build();

    public ThraggBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, "thragg_boss", CONFIG);
    }

    public static AttributeSupplier setAttributes() {
        return createAttributeSupplier(CONFIG);
    }
}