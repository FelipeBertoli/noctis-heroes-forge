package com.noctisheroes.entity.mobs;

import com.noctisheroes.entity.base.AbstractViltrumite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.RawAnimation;

public class ViltrumiteEntity extends AbstractViltrumite {

    private static final int SKIN_COUNT = 6                              ;

    @Override
    protected int getSkinCount() {
        return SKIN_COUNT;
    }

    public ViltrumiteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "viltrumite");
        this.xpReward = 30;
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.6D) // importante pra mobs voadores
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.7D)
                .add(Attributes.ATTACK_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.ARMOR, 0.6D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D).build();
    }

}