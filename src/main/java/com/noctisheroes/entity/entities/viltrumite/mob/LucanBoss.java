package com.noctisheroes.entity.entities.viltrumite.mob;

import com.noctisheroes.entity.entities.viltrumite.base.ViltrumiteBoss;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/**
 * Boss Lucan - Viltrumita médio.
 * Utiliza configuração via ViltrumiteConfig para simplificar código.
 */
public class LucanBoss extends ViltrumiteBoss {

    private static final ViltrumiteConfig CONFIG = new ViltrumiteConfig.Builder()
            .xpReward(60)
            .skinCount(1)
            .maxHealth(300.0)
            .movementSpeed(0.4)
            .flyingSpeed(0.8)
            .attackDamage(20.0)
            .attackKnockback(1.0)
            .attackSpeed(0.5)
            .followRange(50.0)
            .armor(0.9)
            .armorToughness(0.36)
            .knockbackResistance(0.5)
            .randomFlightToggleChance(0.001f)  // Menos mudanças aleatórias
            .combatFlightChance(0.02f)         // Mais agressivo em combate
            .build();

    public LucanBoss(EntityType<? extends Monster> type, Level level) {

        super(type, level, "lucan_boss", CONFIG);
        this.getBossComponent();
    }

    public static AttributeSupplier setAttributes() {
        return createAttributeSupplier(CONFIG);
    }
}