package com.noctisheroes.common.effect;

import com.noctisheroes.entity.NoctisEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectManager {

    private final List<EffectInstance> effects = new ArrayList<>();

    public void addEffect(EffectConfig config) {

        if (!config.stackable) {
            effects.removeIf(e -> e.getType() == config.type);
        }

        effects.add(new EffectInstance(config));
    }

    public void tick(NoctisEntity entity) {

        Iterator<EffectInstance> it = effects.iterator();

        while (it.hasNext()) {
            EffectInstance effect = it.next();

            boolean alive = effect.tick(entity);

            if (!alive) {
                it.remove();
            }
        }
    }

    public boolean hasEffect(EffectType type) {
        return effects.stream()
                .anyMatch(e -> e.config.type == type);
    }
}