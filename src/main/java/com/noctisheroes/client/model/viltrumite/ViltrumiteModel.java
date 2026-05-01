package com.noctisheroes.client.model.viltrumite;

import com.noctisheroes.client.model.NoctisEntityModel;
import com.noctisheroes.entity.entities.viltrumite.mob.ViltrumiteEntity;

/**
 * Modelo do Viltrumita.
 * O arquivo geo fica em: assets/noctisheroes/geo/entity/viltrumite.geo.json
 * As animações ficam em: assets/noctisheroes/animations/viltrumite.animation.json
 */
public class ViltrumiteModel extends NoctisEntityModel<ViltrumiteEntity> {

    public ViltrumiteModel() {
        super("viltrumite", "viltrumite", "viltrumite");
    }
}