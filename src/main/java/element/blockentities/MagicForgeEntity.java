package element.blockentities;

import element.Registration;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Map;

public class MagicForgeEntity extends BlockEntity implements Inventory, Tickable, BlockEntityClientSerializable {
    private static final long TICKS_PER_OPERATION = 10;
    private static String REPAIR_KEY = "repair";
    DefaultedList<ItemStack> inv = DefaultedList.ofSize(1, ItemStack.EMPTY);
    // how much repair power this forge is storing. 1 power = 1 durability repaired
    private int repair;
    // IronAltar that powers this forge. Can be null.
    private IronAltarEntity cached_altar;

    public MagicForgeEntity() {
        super(Registration.BLOCK_ENTITIES.MAGIC_FORGE);
        repair = 0;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        Inventories.fromTag(tag, inv);
        repair = tag.getInt(REPAIR_KEY);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt(REPAIR_KEY, repair);
        Inventories.toTag(tag, inv);
        return super.toTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        // TODO?: do we need to call super here? i imagine not, but :shrug:
        // TODO: is this a mojang bug or am I doing stuff wrong?
        // Inventories.toTag serializes an empty inventory as "Items: []"
        // Inventories.fromTag treats this empty array as a no-op.
        // However, if the array is empty, we want to set all the inv slots as empty,
        // so we default the inv as empty whenever we deserialize.
        inv.clear();
        Inventories.fromTag(tag, inv);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        Inventories.toTag(tag, inv);
        return tag;
    }

    @Override
    public int getInvSize() {
        return inv.size();
    }

    @Override
    public boolean isInvEmpty() {
        return inv.stream().map(ItemStack::isEmpty).reduce(true, (a, b) -> a && b);
    }

    @Override
    public ItemStack getInvStack(int slot) {
        return inv.get(slot);
    }

    @Override
    public ItemStack takeInvStack(int slot, int amount) {
        ItemStack out = inv.get(slot).split(amount);
        markDirty();
        return out;
    }

    @Override
    public ItemStack removeInvStack(int slot) {
        ItemStack out = Inventories.removeStack(inv, slot);
        markDirty();
        return out;
    }

    @Override
    public void setInvStack(int slot, ItemStack stack) {
        inv.set(slot, stack);
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        inv.clear();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.PASS;
        }
        if (hand == Hand.OFF_HAND) {
            return ActionResult.PASS;
        }
        ItemStack inHand = player.getStackInHand(hand);
        ItemStack inInv = getInvStack(0);
        if (canAcceptTool(inHand) && inInv.isEmpty()) {
            setInvStack(0, inHand);
            player.setStackInHand(hand, ItemStack.EMPTY);
            markDirty();
            return ActionResult.SUCCESS;
        } else if (inHand.isEmpty() && !inInv.isEmpty()) {
            player.setStackInHand(hand, inInv);
            removeInvStack(0);
            markDirty();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void tick() {
        if (getWorld().isClient()) {
            return;
        }

        if (getWorld().getTime() % TICKS_PER_OPERATION == 0) {
            ItemStack inInv = getInvStack(0);
            if (!inInv.isEmpty()) {
                int damage = inInv.getDamage();
                if (damage > 0) {
                    if (repair == 0) {
                        IronAltarEntity altar = getAltar();
                        if (altar != null) {
                            if (altar.takeEnergy(1, getPos())) {
                                // TODO: magic numbers :)
                                // 1 iron = 10 energy, 1 energy = 12 repair,
                                // which means that repairing an iron tool is
                                // ~2 iron, which is about 2 cheaper than an anvil
                                // and 1 iron cheaper than making a new pick
                                repair += 12;
                            }
                        }
                    }

                    if (repair > 0) {
                        inInv.setDamage(damage - 1);
                        repair--;
                        markDirty();
                    }
                }
            }
        }
    }

    /**
     * @return an IronAltarEntity that can be used as an energy source,
     *  null if no nearby IronAltarEntity can be found.
     */
    private IronAltarEntity getAltar() {
        // TODO?: improve this
        if (cached_altar == null) {
            double closestDist = Double.MAX_VALUE;
            ChunkPos center = world.getWorldChunk(getPos()).getPos();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    WorldChunk chunk = world.getChunk(center.x + dx, center.z + dz);
                    Map<BlockPos, BlockEntity> entities = chunk.getBlockEntities();
                    for (Map.Entry<BlockPos, BlockEntity> pair : entities.entrySet()) {
                        if (pair.getValue() instanceof IronAltarEntity) {
                            double dist = getPos().getSquaredDistance(pair.getKey());
                            if (dist < closestDist) {
                                cached_altar = (IronAltarEntity)pair.getValue();
                                closestDist = dist;
                            }
                        }
                    }
                }
            }
        }

        return cached_altar;
    }

    /**
     * @return true iff item is a tool this forge can repair
     */
    private boolean canAcceptTool(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.IRON_PICKAXE || item == Items.IRON_AXE ||
            item == Items.IRON_SHOVEL || item == Items.IRON_HOE ||
            item == Items.IRON_SWORD;
    }
}
