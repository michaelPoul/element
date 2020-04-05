package element;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

public class SharedInit implements ModInitializer {
	public static final String MOD_ID = "element";
	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "item_group")).icon(() -> new ItemStack(Registration.ITEMS.MAGIC_STICK)).build();

	@Override
	public void onInitialize() {
		Registration.register();
	}
}
