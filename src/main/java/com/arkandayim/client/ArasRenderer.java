package com.arkandayim.client;

import com.arkandayim.entity.ArasEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ArasRenderer extends MobRenderer<ArasEntity, ArasModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("arkandayim", "textures/entity/aras.png");

    public ArasRenderer(EntityRendererProvider.Context context) {
        super(context, new ArasModel(context.bakeLayer(ClientSetup.ARAS_LAYER)), 0.45F);
        this.addLayer(new ArasEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ArasEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(ArasEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected void scale(ArasEntity entity, PoseStack poseStack, float partialTicks) {
        // A player shaped model is two blocks tall, so this reaches the 2.8 block hitbox.
        poseStack.scale(1.38F, 1.38F, 1.38F);
        float progress = entity.getDeathAnimProgress(partialTicks);
        if (progress > 0.0F) {
            float scale = Mth.clamp(1.0F - progress * 0.96F, 0.03F, 1.0F);
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotationDegrees(progress * 720.0F));
            return;
        }
        if (entity.getState() == ArasEntity.STATE_FORMING) {
            float grow = Mth.clamp(1.0F - (entity.getFormTicks() - partialTicks) / 52.0F, 0.05F, 1.0F);
            poseStack.scale(grow, grow, grow);
            poseStack.mulPose(Axis.YP.rotationDegrees((1.0F - grow) * 520.0F));
            poseStack.translate(0.0D, (1.0F - grow) * -0.4D, 0.0D);
            return;
        }
        if (entity.isFlying()) {
            poseStack.translate(0.0D, 0.10D + Mth.sin((entity.tickCount + partialTicks) * 0.25F) * 0.06D, 0.0D);
        }
    }
}
