package com.noctisheroes.entity.handlers;

import com.noctisheroes.entity.NoctisEntity;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class MeleeAttackHandler <T extends NoctisEntity>{

    private boolean lastAttackRight = false;

    public PlayState handle(AnimationState<T> event, T entity) {

        if (entity.swinging && event.getController().getAnimationState() == AnimationController.State.STOPPED) {

            event.getController().forceAnimationReset();

            if (lastAttackRight) {
                event.setAndContinue(NoctisEntity.LEFT_ATTACK_ANIM);
            } else {
                event.setAndContinue(NoctisEntity.RIGHT_ATTACK_ANIM );
            }
            lastAttackRight = !lastAttackRight;
            entity.swinging = false;


        }
        return PlayState.CONTINUE;
    }
}
