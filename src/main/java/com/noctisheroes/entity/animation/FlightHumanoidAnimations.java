package com.noctisheroes.entity.animation;

import net.minecraft.util.RandomSource;
import software.bernie.geckolib.core.animation.RawAnimation;

public class FlightHumanoidAnimations
        extends HumanoidAnimations {

    private static final RawAnimation LEVITATE_IDLE =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.levitate_idle");

    private static final RawAnimation LEVITATE_FLIGHT =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.levitate_flight");

    private static final RawAnimation FLIGHT_START =
            RawAnimation.begin()
                    .thenPlay("animation.humanoid.flight_start");

    private static final RawAnimation FLIGHT_STOP =
            RawAnimation.begin()
                    .thenPlay("animation.humanoid.flight_stop");

    private static final RawAnimation HUNT_FLIGHT_1 =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.hunt_flight1");

    private static final RawAnimation HUNT_FLIGHT_2 =
            RawAnimation.begin()
                    .thenLoop("animation.humanoid.hunt_flight2");


    private RawAnimation cachedHuntFlight;

    @Override
    public RawAnimation getAnimation(
            AnimationKey key
    ) {

        return switch (key) {

            case LEVITATE_IDLE ->
                    LEVITATE_IDLE;

            case LEVITATE_FLIGHT ->
                    LEVITATE_FLIGHT;

            case FLIGHT_START ->
                    FLIGHT_START;

            case FLIGHT_STOP ->
                    FLIGHT_STOP;

            case HUNT_FLIGHT -> {

                if (cachedHuntFlight == null) {

                    cachedHuntFlight =
                            RandomSource.create().nextBoolean()
                                    ? HUNT_FLIGHT_1
                                    : HUNT_FLIGHT_2;
                }

                yield cachedHuntFlight;
            }

            default ->
                    super.getAnimation(key);
        };
    }

    public void resetHuntFlightVariation() {
        cachedHuntFlight = null;
    }
}