package net.blancworks.figura.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerData;
import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.access.ModelPartAccess;
import net.blancworks.figura.access.PlayerEntityModelAccess;
import net.blancworks.figura.trust.PlayerTrustManager;
import net.blancworks.figura.trust.TrustContainer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import net.minecraft.entity.LivingEntity;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> implements PlayerEntityModelAccess {

    @Shadow
    @Final
    public ModelPart jacket;
    @Shadow
    @Final
    public ModelPart leftPants;
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    public ModelPart rightPants;
    @Shadow
    @Final
    private ModelPart ear;
    @Shadow
    private List<ModelPart> parts;
    private HashSet<ModelPart> disabled_parts = new HashSet<ModelPart>();
    
    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        try {
            PlayerData playerData = FiguraMod.getCurrData();
            if(playerData == null) {
                super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                return;
            }

            PlayerDataManager.checkForPlayerDataRefresh(playerData);
            TrustContainer trustData = playerData.getTrustContainer();
            
            if (playerData != null && playerData.script != null && playerData.script.vanillaModifications != null && trustData.getBoolSetting(PlayerTrustManager.allowVanillaModID)) {
                playerData.script.applyCustomValues((PlayerEntityModel) (Object) this);
            } else {
                for (ModelPart part : parts) {
                    ModelPartAccess mpa = (ModelPartAccess) (Object) part;
                    mpa.setAdditionalPos(new Vec3f());
                    mpa.setAdditionalRot(new Vec3f());
                }


                resetModelPartAdditionalValues(head);
                resetModelPartAdditionalValues(hat);
                resetModelPartAdditionalValues(body);
                resetModelPartAdditionalValues(jacket);
                resetModelPartAdditionalValues(rightArm);
                resetModelPartAdditionalValues(leftArm);
                resetModelPartAdditionalValues(rightLeg);
                resetModelPartAdditionalValues(leftLeg);
                
                resetModelPartAdditionalValues(rightSleeve);
                resetModelPartAdditionalValues(leftSleeve);
                resetModelPartAdditionalValues(rightPants);
                resetModelPartAdditionalValues(leftPants);
            }

            super.render(matrices, vertices, light, overlay, red, green, blue, alpha);

            if (playerData != null) {
                if (playerData.model != null) {
                    if (playerData.texture == null || playerData.texture.ready == false) {
                        return;
                    }
                    //We actually wanna use this custom vertex consumer, not the one provided by the render arguments.
                    VertexConsumer actualConsumer = FiguraMod.vertex_consumer_provider.getBuffer(RenderLayer.getEntityCutout(playerData.texture.id));
                    playerData.model.render((PlayerEntityModel<?>) (Object) this, matrices, actualConsumer, light, overlay, red, green, blue, alpha);
                }
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.log(Level.ERROR, e);
        }
    }

    @Inject(at = @At("TAIL"), method = "setVisible(Z)V")
    public void setVisible(boolean visible, CallbackInfo ci) {
        PlayerEntityModel mdl = (PlayerEntityModel) (Object) this;

        for (ModelPart part : disabled_parts) {
            part.visible = false;
            if(part == hat)
                ear.visible = part.visible;
        }
    }

    @Override
    public HashSet<ModelPart> getDisabledParts() {
        return disabled_parts;
    }

    public void resetModelPartAdditionalValues(ModelPart part){
        ModelPartAccess mpa = (ModelPartAccess) (Object) part;
        mpa.setAdditionalPos(new Vec3f());
        mpa.setAdditionalRot(new Vec3f());
    }
}
