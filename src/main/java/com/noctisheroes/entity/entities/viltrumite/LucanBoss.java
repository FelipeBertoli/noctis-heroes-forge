package com.noctisheroes.entity.entities.viltrumite;

import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.attribute.AttributeManager;
import com.noctisheroes.entity.entities.base.ViltrumiteBoss;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
/**
 * Boss Lucan - Viltrumita médio.
 * Utiliza configuração via ViltrumiteConfig para simplificar código.
 */
public class LucanBoss extends ViltrumiteBoss {


    private static final AttributeConfig ATTRIBUTES = new AttributeConfig.Builder()
            .maxHealth(300.0)
            .movementSpeed(0.4)
            .flyingSpeed(0.8)
            .attackDamage(20.0)
            .attackKnockback(1.0)
            .attackSpeed(0.5)
            .followRange(50.0)
            .armor(0.9)
            .armorToughness(0.36)
            .explosionResistance(0.5f)
            .fireResistance(0.5f)
            .projectileResistance(0.5f)
            .build();

    private static final EntityConfig CONFIG =
            new EntityConfig.Builder()
                    .xpReward(100)
                    .skinCount(1)
                    .build();

    public LucanBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, "lucan_boss", ATTRIBUTES, CONFIG);
        this.getBossComponent();
    }

    public static AttributeSupplier createAttributes() {
        return new AttributeManager(ATTRIBUTES).build();
    }
}