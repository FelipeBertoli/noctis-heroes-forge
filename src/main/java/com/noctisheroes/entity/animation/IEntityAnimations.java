package com.noctisheroes.entity.animation;

import software.bernie.geckolib.core.animation.RawAnimation;

public interface IEntityAnimations {

    RawAnimation getAnimation(AnimationKey key);
}