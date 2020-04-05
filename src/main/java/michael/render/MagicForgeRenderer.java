package michael.render;

import michael.blockentities.MagicForgeEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;

public class MagicForgeRenderer extends BlockEntityRenderer<MagicForgeEntity> {
    public MagicForgeRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(MagicForgeEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack inv = blockEntity.getInvStack(0);
        if (!inv.isEmpty()) {
            matrices.push();

            // move to the block center and a bit up
            matrices.translate(0.5, 1.15, 0.5);
            // rotate slowly
            float DEG_PER_TICK = (float)(360 / 200); // 1 rotation every 10 seconds
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((blockEntity.getWorld().getTime() + tickDelta) * DEG_PER_TICK));
            int itemLight = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(inv, ModelTransformation.Mode.GROUND, itemLight, overlay, matrices, vertexConsumers);

            matrices.pop();
        }
    }
}
