package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AbilityManager<T extends NoctisEntity> {

    private final List<iNoctisAbility<T>> abilities = new ArrayList<>();
    private iNoctisAbility<T> currentAbility;
    private int cooldownTicks = 0;

    public void register(iNoctisAbility<T> ability) {
        abilities.add(ability);
    }

    public void tick(T entity) {

        // cooldown global
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        // executando habilidade atual
        if (currentAbility != null) {
            currentAbility.tick(entity);

            if (currentAbility.isFinished(entity)) {
                currentAbility.stop(entity);
                cooldownTicks = currentAbility.getCooldown();
                currentAbility = null;
            }
            return;
        }

        if (cooldownTicks <= 0) {

            abilities.stream()
                    .sorted(Comparator.comparingInt(iNoctisAbility<T>::getPriority).reversed())
                    .forEach(ability -> {

                        if (currentAbility != null) return;

                        if (!ability.canUse(entity)) return;

                        // 🔥 Checagem de recurso
                        if (ability instanceof IResourceAbility<?> resourceAbility) {
                            IResourceAbility<T> casted = (IResourceAbility<T>) resourceAbility;

                            if (!casted.hasResource(entity)) return;
                        }

                        currentAbility = ability;
                        ability.start(entity);

                        // 🔥 Consumo de recurso
                        if (ability instanceof IResourceAbility<?> resourceAbility) {
                            IResourceAbility<T> casted = (IResourceAbility<T>) resourceAbility;

                            casted.consumeResource(entity);
                        }
                    });
        }
    }

    public boolean isUsingAbility() {
        return currentAbility != null;
    }

    public iNoctisAbility<T> getCurrentAbility() {
        return currentAbility;
    }
}