package com.noctisheroes.entity.animation;

import com.noctisheroes.entity.animation.AnimationKey;
import com.noctisheroes.entity.animation.IEntityAnimations;
import software.bernie.geckolib.core.animation.RawAnimation;

public class HumanoidAnimations
        implements IEntityAnimations {

    private static final RawAnimation IDLE =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.idle");

    private static final RawAnimation WALK =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.walk");

    private static final RawAnimation RIGHT_ATTACK =
            RawAnimation.begin()
                    .thenPlay("animation.humanoid.right_attack");

    private static final RawAnimation LEFT_ATTACK =
            RawAnimation.begin()
                    .thenPlay("animation.humanoid.left_attack");

    @Override
    public RawAnimation getAnimation(
            AnimationKey key
    ) {

        return switch (key) {

            case IDLE ->
                    IDLE;

            case WALK ->
                    WALK;

            case RIGHT_ATTACK ->
                    RIGHT_ATTACK;

            case LEFT_ATTACK ->
                    LEFT_ATTACK;

            default ->
                    null;
        };
    }
}