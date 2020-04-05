package element.blockentities;

import io.netty.buffer.Unpooled;
import element.Registration;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IronAltarEntity extends BlockEntity {
    private static final String ENERGY_KEY = "energy";
    private int energy;

    public IronAltarEntity() {
        super(Registration.BLOCK_ENTITIES.IRON_ALTAR);
        energy = 0;
    }

    public ItemStack addIron(ItemStack ironStack) {
        energy += ironStack.getCount() * 10;
        markDirty();
        return ItemStack.EMPTY;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt(ENERGY_KEY, energy);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        energy = tag.getInt(ENERGY_KEY);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.PASS;
        }
        ItemStack inHand = player.getStackInHand(hand);
        // maybe == for the item comparison, since they should be flyweights.
        // That's what ItemStack.isItemEqual does :shrug:
        if (inHand.getItem().equals(Items.IRON_INGOT)) {
            energy += inHand.getCount() * 10;
            markDirty();
            player.setStackInHand(hand, ItemStack.EMPTY);
            return ActionResult.SUCCESS;
        } else if (inHand.getItem().equals(Registration.ITEMS.MAGIC_STICK)) {
            player.sendMessage(new LiteralText("Energy: " + this.energy));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    /**
     * Tries to take energy from this altar.
     * @param amount The amount of energy to try extracting
     * @param pos The block trying to pull energy. TODO this is hacky
     * @return true if the energy was successfully extracted, false otherwise.
     */
    public boolean takeEnergy(int amount, BlockPos pos) {
        if (energy >= amount) {
            energy -= amount;
            markDirty();
            // render fancy particle
            // TODO? move this code someplace more render specific
            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            data.writeBlockPos(getPos());
            data.writeBlockPos(pos);
            data.writeInt(amount);
            PlayerStream.watching(world, pos).forEach((player) -> {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player,
                    Registration.PACKETS.TAKE_ENERGY, data);
            });
            return true;
        }
        return false;
    }
}
