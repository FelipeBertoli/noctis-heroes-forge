package com.noctisheroes.entity.components;

public class ResourceComponent {

    private float value = 0f;
    private float max = 100f;

    private float passiveGain = 0.02f;

    public void tick() {
        add(passiveGain);
    }

    public void add(float amount) {
        value = Math.min(max, value + amount);
    }

    public void consume(float amount) {
        value = Math.max(0, value - amount);
    }

    public float getPercent() {
        return value / max;
    }

    public float getValue() {
        return value;
    }

    public void set(float v) {
        value = Math.max(0, Math.min(max, v));
    }
}