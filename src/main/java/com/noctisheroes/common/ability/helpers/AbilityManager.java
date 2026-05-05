package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;

import java.util.HashMap;
import java.util.Map;

public class AbilityManager<T extends NoctisEntity> {

    private final Map<String, NoctisAbility<T>> abilities = new HashMap<>();
    private NoctisAbility<T> currentAbility;
    private int cooldownTicks = 0;

    public void register(NoctisAbility<T> ability) {
        abilities.put(ability.getId(), ability);
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

        // tentar iniciar nova habilidade
        if (cooldownTicks <= 0) {
            for (NoctisAbility<T> ability : abilities.values()) {
                if (ability.canUse(entity)) {
                    currentAbility = ability;
                    ability.start(entity);
                    break;
                }
            }
        }
    }

    public boolean isUsingAbility() {
        return currentAbility != null;
    }

    public String getCurrentAbilityId() {
        return currentAbility != null ? currentAbility.getId() : "";
    }

    public NoctisAbility<T> getCurrentAbility() {
        return currentAbility;
    }

}