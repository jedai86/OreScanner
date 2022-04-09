package net.jedai.orescanner.config;

import com.google.common.collect.Lists;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.jedai.orescanner.OreScanner.MOD_ID;

@Config(name = MOD_ID)
public class ModConfig implements ConfigData {

    @Override
    public void validatePostLoad() {
        Set<String> validatedGroups = new HashSet<>();

        if (additionalBlocks.isEmpty()) {
            List<String> defaults = Lists.newArrayList(
                    "minecraft:ancient_debris"
            );
            validatedGroups.addAll(defaults);
        }
        validatedGroups.addAll(additionalBlocks);
        additionalBlocks = Lists.newArrayList(validatedGroups);
    }

    @Comment("Minimum ore scanner radius")
    public int scanner_min_radius = 4;
    @Comment("Maximum ore scanner radius")
    public int scanner_max_radius = 10;
    @Comment("How much energy ore scanner consumes for each use with minimum radius")
    public int energyCost = 100;
    @Comment("The energy capacity expressed in E")
    public int maxCharge = 10000;
    @Comment("Additional blocks to detect as ore, only block ids accepted")
    public List<String> additionalBlocks = new ArrayList<>();
}


