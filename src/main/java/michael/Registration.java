package michael;

import michael.blockentities.IronAltarEntity;
import michael.blockentities.MagicForgeEntity;
import michael.blocks.IronAltar;
import michael.blocks.MagicForge;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Registration {
    // The My... classes store the blocks/items/blockentities/etc. for this mod.
    // On construction, they create and register the instances.
    public static class MyBlocks {
        public final Block IRON_ALTAR = new IronAltar(FabricBlockSettings.of(Material.METAL).hardness(2).build());
        public final Block MAGIC_FORGE = new MagicForge(FabricBlockSettings.of(Material.METAL).hardness(3).build());

        public MyBlocks() {
            Registry.register(Registry.BLOCK, new Identifier(SharedInit.MOD_ID, "iron_altar"), IRON_ALTAR);
            Registry.register(Registry.BLOCK, new Identifier(SharedInit.MOD_ID, "magic_forge"), MAGIC_FORGE);
        }
    }

    public static class MyItems {
        public Item IRON_ALTAR;
        public Item MAGIC_FORGE;
        public Item MAGIC_STICK;

        public MyItems(MyBlocks blocks) {
            IRON_ALTAR = new BlockItem(blocks.IRON_ALTAR, new Item.Settings().group(SharedInit.ITEM_GROUP));
            Registry.register(Registry.ITEM, new Identifier(SharedInit.MOD_ID, "iron_altar"), IRON_ALTAR);
            MAGIC_FORGE = new BlockItem(blocks.MAGIC_FORGE, new Item.Settings().group(SharedInit.ITEM_GROUP));
            Registry.register(Registry.ITEM, new Identifier(SharedInit.MOD_ID, "magic_forge"), MAGIC_FORGE);
            MAGIC_STICK = new Item(new Item.Settings().group(SharedInit.ITEM_GROUP));
            Registry.register(Registry.ITEM, new Identifier(SharedInit.MOD_ID, "magic_stick"), MAGIC_STICK);
        }
    }

    public static class MyBlockEntities {
        public BlockEntityType<IronAltarEntity> IRON_ALTAR;
        public BlockEntityType<MagicForgeEntity> MAGIC_FORGE;

        public MyBlockEntities(MyBlocks blocks) {
            IRON_ALTAR = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                    new Identifier(SharedInit.MOD_ID, "iron_altar"),
                    BlockEntityType.Builder.create(IronAltarEntity::new, blocks.IRON_ALTAR).build(null));
            MAGIC_FORGE = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                    new Identifier(SharedInit.MOD_ID, "magic_forge"),
                    BlockEntityType.Builder.create(MagicForgeEntity::new, blocks.MAGIC_FORGE).build(null));
        }
    }

    public static class MyPacketIds {
        public Identifier TAKE_ENERGY;

        public MyPacketIds() {
            TAKE_ENERGY = new Identifier(SharedInit.MOD_ID, "take_energy");
        }
    }

    public static MyBlocks BLOCKS;
    public static MyItems ITEMS;
    public static MyBlockEntities BLOCK_ENTITIES;
    public static MyPacketIds PACKETS;

    public static void register() {
        BLOCKS = new MyBlocks();
        ITEMS = new MyItems(BLOCKS);
        BLOCK_ENTITIES = new MyBlockEntities(BLOCKS);
        PACKETS = new MyPacketIds();
    }
}
