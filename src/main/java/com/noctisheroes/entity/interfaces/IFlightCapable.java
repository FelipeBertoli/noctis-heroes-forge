package com.noctisheroes.entity.interfaces;


import com.noctisheroes.entity.ai.flight.FlightState;

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

}