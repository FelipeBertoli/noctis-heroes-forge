package com.noctisheroes.entity.interfaces;


import com.noctisheroes.entity.ai.states.FlightState;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;

public interface IFlightCapable{

    /**
     * Obtém estado de voo atual.
     */
    FlightState getFlightState();

    /**
     * Define novo estado de voo (com validação).
     */
    void setFlightState(FlightState state);

    /**
     * Force set sem validação (backup).
     */
    void forceSetFlightState(FlightState state);

    /**
     * Obtém navegação para voo.
     */
    FlyingPathNavigation getFlyingNavigation();

    /**
     * Chance de iniciar voo em combate (0.0 - 1.0).
     */
    float getCombatFlightChance();

    /**
     * Chance de toggle aleatório de voo (0.0 - 1.0).
     */
    float getRandomFlightToggleChance();
}