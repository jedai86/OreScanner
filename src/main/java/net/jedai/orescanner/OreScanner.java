package net.jedai.orescanner;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.jedai.orescanner.config.ModConfig;
import net.jedai.orescanner.item.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OreScanner implements ModInitializer {
	public static final String MOD_ID = "orescanner";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final ItemGroup SCANNER_TAB = FabricItemGroupBuilder.build(new Identifier(OreScanner.MOD_ID, "orescannertab"),
			() -> new ItemStack(ModItems.ORE_SCANNER));

	@Override
	public void onInitialize() {
		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		ModItems.registerModItems();

	}
}
