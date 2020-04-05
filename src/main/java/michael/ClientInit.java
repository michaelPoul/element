package michael;

import michael.render.MagicForgeRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(Registration.PACKETS.TAKE_ENERGY,
            (context, data) -> {
                // translate to the center of the blocks
                BlockPos from = data.readBlockPos();
                BlockPos to = data.readBlockPos();
                int amount = data.readInt();
                // TODO: move this function body elsewhere lol
                context.getTaskQueue().execute(
                    () -> {
                        Vec3d from2 = new Vec3d(from).add(0.5, 2, 0.5);
                        Vec3d to2 = new Vec3d(to).add(0.5, 1.5, 0.5);
                        Vec3d vel = from2.subtract(to2);
                        // TODO?: add my own particles instead of using vanilla
                        // ones
                        MinecraftClient.getInstance().particleManager.addParticle(
                                ParticleTypes.NAUTILUS, to2.getX(), to2.getY(),
                                to2.getZ(), vel.getX(), vel.getY(), vel.getZ());
                    }
                );
            }
        );

        BlockEntityRendererRegistry.INSTANCE.register(Registration.BLOCK_ENTITIES.MAGIC_FORGE, MagicForgeRenderer::new);
    }
}
