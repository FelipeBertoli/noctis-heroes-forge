package com.noctisheroes.client.model;

import com.noctisheroes.entity.mobs.ViltrumiteEntity;

/**
 * Modelo do Viltrumita.
 * O arquivo geo fica em: assets/noctisheroes/geo/entity/viltrumite.geo.json
 * As animações ficam em: assets/noctisheroes/animations/viltrumite.animation.json
 */
public class ViltrumiteModel extends AbstractMobModel<ViltrumiteEntity> {

    public ViltrumiteModel() {
        super("viltrumite", "viltrumite", "viltrumite");
    }
}