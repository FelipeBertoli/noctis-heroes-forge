package com.noctisheroes.entity.animation;

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

    private static final RawAnimation BLOCK =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.block");


    @Override
    public RawAnimation getAnimation(AnimationKey key) {

        return switch (key) {

            case IDLE ->
                    IDLE;

            case WALK ->
                    WALK;

            case RIGHT_ATTACK ->
                    RIGHT_ATTACK;

            case LEFT_ATTACK ->
                    LEFT_ATTACK;

            case BLOCK ->
                    BLOCK;

            default ->
                    null;
        };
    }
}