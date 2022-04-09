package net.jedai.orescanner.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.jedai.orescanner.OreScanner;
import net.jedai.orescanner.item.custom.OreScannerItem;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
    public static final Item ORE_SCANNER = registerItem("ore_scanner",
            new OreScannerItem(new FabricItemSettings().group(OreScanner.SCANNER_TAB).maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(OreScanner.MOD_ID, name), item);
    }

    public static void registerModItems() {
        OreScanner.LOGGER.info("Registering Mod Items for " + OreScanner.MOD_ID);
    }
}
