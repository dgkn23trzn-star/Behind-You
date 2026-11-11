package com.arkandayim.client;

import com.arkandayim.entity.ArasEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A player shaped, pitch black figure: two legs, two arms, a head with a jaw that
 * opens when it screams. It stands and breathes, runs with a real arm and leg swing,
 * screams with its head thrown back and hovers with its arms spread.
 */
public class ArasModel extends EntityModel<ArasEntity> {

    public final ModelPart torso;
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart jaw;
    public final ModelPart eyes;
    public final ModelPart mouth;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public ArasModel(ModelPart root) {
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
        this.torso = root.getChild("torso");
        this.body = this.torso.getChild("body");
        this.head = this.torso.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.armLeft = this.torso.getChild("arm_left");
        this.armRight = this.torso.getChild("arm_right");
        this.eyes = root.getChild("eyes");
        this.mouth = root.getChild("mouth");
        this.eyes.skipDraw = true;
        this.mouth.skipDraw = true;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Standard player proportions, one and a half blocks of legs below the neck.
        root.addOrReplaceChild("leg_left", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.02F)),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.02F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition torso = root.addOrReplaceChild("torso",
                CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        torso.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.06F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.04F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.2F, 0.0F, -4.0F, 6.4F, 1.6F, 3.6F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.1F, 0.0F));

        torso.addOrReplaceChild("arm_left", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.02F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        torso.addOrReplaceChild("arm_right", CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.02F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Emissive parts: eyes always, mouth only while screaming.
        root.addOrReplaceChild("eyes", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.4F, -5.9F, -4.3F, 2.2F, 1.7F, 0.3F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(1.2F, -5.9F, -4.3F, 2.2F, 1.7F, 0.3F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("mouth", CubeListBuilder.create()
                        .texOffs(8, 0).addBox(-2.6F, -3.3F, -4.15F, 5.2F, 1.5F, 0.25F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ArasEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float time = ageInTicks;
        boolean scream = entity.isScreaming();
        boolean fly = entity.isFlying();
        boolean moving = limbSwingAmount > 0.06F;
        float swing = Mth.cos(limbSwing * 0.6662F) * Math.min(limbSwingAmount, 1.6F);

        this.torso.xRot = 0.0F;
        this.torso.yRot = 0.0F;
        this.torso.zRot = 0.0F;
        this.torso.y = 0.0F;
        this.body.y = 0.0F;

        if (scream) {
            this.torso.xRot = -0.14F;
            this.torso.zRot = Mth.sin(time * 2.4F) * 0.035F;
            this.torso.y = -0.4F + Mth.sin(time * 3.1F) * 0.25F;
            this.head.xRot = -0.62F + Mth.sin(time * 2.0F) * 0.06F;
            this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            this.head.zRot = Mth.sin(time * 2.7F) * 0.07F;
            this.jaw.xRot = 0.85F + Mth.sin(time * 3.4F) * 0.12F;
            this.armLeft.xRot = -0.55F;
            this.armRight.xRot = -0.55F;
            this.armLeft.zRot = -1.25F + Mth.sin(time * 2.2F) * 0.07F;
            this.armRight.zRot = 1.25F - Mth.sin(time * 2.2F) * 0.07F;
            this.legLeft.xRot = 0.12F + Mth.sin(time * 1.6F) * 0.04F;
            this.legRight.xRot = 0.12F - Mth.sin(time * 1.6F) * 0.04F;
            this.legLeft.zRot = 0.03F;
            this.legRight.zRot = -0.03F;
        } else if (fly) {
            this.torso.xRot = 0.30F;
            this.torso.zRot = Mth.sin(time * 0.18F) * 0.05F;
            this.head.xRot = headPitch * Mth.DEG_TO_RAD + 0.18F;
            this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            this.head.zRot = Mth.sin(time * 0.15F) * 0.05F;
            this.jaw.xRot = 0.22F;
            this.armLeft.xRot = -0.35F + Mth.sin(time * 0.35F) * 0.10F;
            this.armRight.xRot = -0.35F - Mth.sin(time * 0.35F) * 0.10F;
            this.armLeft.zRot = -1.30F;
            this.armRight.zRot = 1.30F;
            this.legLeft.xRot = -0.42F + Mth.sin(time * 0.30F) * 0.08F;
            this.legRight.xRot = -0.30F - Mth.sin(time * 0.30F) * 0.08F;
        } else if (moving) {
            this.torso.xRot = 0.05F + Math.min(limbSwingAmount, 1.0F) * 0.20F;
            this.torso.zRot = Mth.sin(limbSwing * 0.6662F) * 0.02F;
            this.body.y = Math.abs(Mth.cos(limbSwing * 0.6662F)) * 0.35F * Math.min(limbSwingAmount, 1.0F);
            this.head.xRot = headPitch * Mth.DEG_TO_RAD - 0.10F;
            this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            this.head.zRot = 0.0F;
            this.jaw.xRot = 0.10F + Mth.sin(time * 0.55F) * 0.04F;
            this.armLeft.xRot = -swing * 1.10F - 0.05F;
            this.armRight.xRot = swing * 1.10F - 0.05F;
            this.armLeft.zRot = -0.07F;
            this.armRight.zRot = 0.07F;
            this.legLeft.xRot = swing * 1.25F;
            this.legRight.xRot = -swing * 1.25F;
            this.legLeft.zRot = 0.0F;
            this.legRight.zRot = 0.0F;
        } else {
            // Idle: slow breathing, tiny sway, arms hanging, head moving now and then.
            float breath = Mth.sin(time * 0.05F);
            this.torso.xRot = 0.04F + breath * 0.02F;
            this.torso.zRot = Mth.sin(time * 0.031F) * 0.02F;
            this.body.y = breath * 0.25F;
            this.head.xRot = headPitch * Mth.DEG_TO_RAD + 0.06F + breath * 0.015F;
            this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD + Mth.sin(time * 0.02F) * 0.10F;
            this.head.zRot = Mth.sin(time * 0.045F) * 0.035F;
            this.jaw.xRot = 0.03F + Math.max(0.0F, Mth.sin(time * 0.04F)) * 0.05F;
            this.armLeft.xRot = 0.05F + Mth.sin(time * 0.05F) * 0.05F;
            this.armRight.xRot = 0.05F - Mth.sin(time * 0.05F) * 0.05F;
            this.armLeft.zRot = -0.06F;
            this.armRight.zRot = 0.06F;
            this.legLeft.xRot = 0.0F;
            this.legRight.xRot = 0.0F;
            this.legLeft.zRot = 0.02F;
            this.legRight.zRot = -0.02F;
        }

        this.eyes.xRot = this.torso.xRot + this.head.xRot;
        this.eyes.yRot = this.head.yRot;
        this.eyes.zRot = this.torso.zRot + this.head.zRot;
        this.eyes.y = this.torso.y;
        this.mouth.xRot = this.eyes.xRot;
        this.mouth.yRot = this.eyes.yRot;
        this.mouth.zRot = this.eyes.zRot;
        this.mouth.y = this.torso.y + (scream ? this.jaw.xRot * 0.9F : 0.0F);
        this.mouth.visible = scream;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                               com.mojang.blaze3d.vertex.VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.torso.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.legLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.legRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
