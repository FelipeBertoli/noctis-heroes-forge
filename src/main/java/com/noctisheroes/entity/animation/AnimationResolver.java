package com.noctisheroes.entity.animation;

import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.flight.FlightState;
import com.noctisheroes.entity.interfaces.IFlightCapable;

public class AnimationResolver {

    public static AnimationKey resolveMovement(
            NoctisEntity entity
    ) {

        if (entity instanceof IFlightCapable flight) {

            FlightState state = flight.getFlightState();

            return switch (state) {

                case FLIGHT_START -> AnimationKey.FLIGHT_START;
                case FLIGHT_STOP -> AnimationKey.FLIGHT_STOP;
                case HUNT_FLIGHT -> AnimationKey.HUNT_FLIGHT;
                case LEVITATE -> {
                    boolean moving = entity.getDeltaMovement().lengthSqr() > 0.002;
                    yield moving ? AnimationKey.LEVITATE_FLIGHT : AnimationKey.LEVITATE_IDLE;
                }
                case GROUNDED -> {
                    boolean moving = entity.getDeltaMovement().horizontalDistanceSqr() > 0.002;
                    yield moving ? AnimationKey.WALK : AnimationKey.IDLE;
                }
                default -> {
                    boolean moving = entity.getDeltaMovement().horizontalDistanceSqr() > 0.002;
                    yield moving ? AnimationKey.WALK : AnimationKey.IDLE;
                }
            };
        }

        // Se não é IFlightCapable, resolve como movimento no chão
        boolean moving = entity.getDeltaMovement()
                .horizontalDistanceSqr() > 0.002;

        return moving ? AnimationKey.WALK : AnimationKey.IDLE;
    }
}