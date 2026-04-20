package com.noctisheroes.client.model;


import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.ViltrumiteEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ViltrumiteModel<T extends ViltrumiteEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation(NoctisHeroes.MODID, "viltrumite"),
                    "main"
            );

    public ViltrumiteModel(ModelPart root) {
        super(root);
    }

    // Define o layout do modelo — usa o humanoid padrão (mesma UV do Steve 64x64)
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(mesh, 64, 64);
    }
}