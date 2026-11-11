package com.arkandayim.client;

import com.arkandayim.entity.ArasEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ArasEyesLayer extends RenderLayer<ArasEntity, ArasModel> {

    private static final RenderType EYES =
            RenderType.eyes(new ResourceLocation("arkandayim", "textures/entity/aras_eyes.png"));

    public ArasEyesLayer(RenderLayerParent<ArasEntity, ArasModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArasEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ArasModel model = this.getParentModel();
        VertexConsumer consumer = buffer.getBuffer(EYES);
        model.eyes.skipDraw = false;
        model.eyes.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        model.eyes.skipDraw = true;
        if (entity.isScreaming()) {
            model.mouth.skipDraw = false;
            model.mouth.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            model.mouth.skipDraw = true;
        }
    }
}
