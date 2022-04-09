package net.jedai.orescanner.item.custom;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jedai.orescanner.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import reborncore.common.powerSystem.RcEnergyItem;
import reborncore.common.powerSystem.RcEnergyTier;
import reborncore.common.util.ItemUtils;
import techreborn.utils.InitUtils;

import java.util.*;

import static net.minecraft.util.ActionResult.SUCCESS;

public class OreScannerItem extends Item implements RcEnergyItem {

    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    public final long energyCost = config.energyCost;
    public final long maxCharge = config.maxCharge;
    public final int min_radius = config.scanner_min_radius;
    public final int max_radius = config.scanner_max_radius;
    public List<String> additionalBlocks = config.additionalBlocks;

    public OreScannerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld().isClient()) {

            PlayerEntity player = context.getPlayer();
            ItemStack stack = context.getStack();
            BlockPos positionClicked = context.getBlockPos();
            World world = context.getWorld();

            if(Screen.hasControlDown()) {
                setRadius(stack);
                player.sendMessage(new LiteralText(new TranslatableText("item.orescanner.scanner.radius").getString() + getRadius(context.getStack())), false);
            }
            else if(Screen.hasShiftDown()) {
                if (isOre(world, positionClicked)) {
                    String selected_ore = world.getBlockState(positionClicked).getBlock().asItem().getName().getString();
                    stack.getOrCreateNbt().putString("ore", selected_ore);
                    player.sendMessage(new LiteralText(new TranslatableText("item.orescanner.scanner.selected_ore").getString() + selected_ore), false);
                }
            }
            else {
                if (getStoredEnergy(stack) >= getEnergyCost(stack)) {
                    boolean foundBlock = false;
                    ArrayList<String> blocksFound = new ArrayList<>();
                    int x = positionClicked.getX();
                    int y = positionClicked.getY();
                    int z = positionClicked.getZ();
                    int r = getRadius(stack);

                    if(stack.getNbt().contains("ore")) {
                        foundBlock = findOre(world, stack, player, positionClicked);

                    } else {
                        for (int i = x - r; i <= x + r; i++) {
                            for (int j = y - r; j <= y + r; j++) {
                                for (int k = z - r; k <= z + r; k++) {
                                    BlockPos position = new BlockPos(i, j, k);
                                    if (isOre(world, position)) {
                                        Block blockBelow = world.getBlockState(position).getBlock();
                                        blocksFound.add(blockBelow.asItem().getName().getString());
                                    }
                                }
                            }
                        }
                        if (!blocksFound.isEmpty()) {
                            foundBlock = true;
                            outputValuableBlocks(player, countOres(blocksFound));
                        }
                    }

                    if (!foundBlock) {
                        player.sendMessage(new TranslatableText("item.orescanner.scanner.no_valuables"), false);
                    }
                    tryUseEnergy(stack, getEnergyCost(stack));
                } else {
                    player.sendMessage(new TranslatableText("item.orescanner.scanner.no_energy"), false);
                }
            }
        }
        return SUCCESS;
    }

    private boolean findOre(World world, ItemStack stack, PlayerEntity player, BlockPos positionClicked) {
        int x = positionClicked.getX();
        int y = positionClicked.getY();
        int z = positionClicked.getZ();
        int r = getRadius(stack);
        for (int rad = 0; rad <= r; rad++) {
            for (int i = x - rad; i <= x + rad; i++) {
                for (int j = y - rad; j <= y + rad; j++) {
                    for (int k = z - rad; k <= z + rad; k++) {
                        BlockPos position = new BlockPos(i, j, k);
                        String blockName = world.getBlockState(position).getBlock().asItem().getName().getString();
                        if (blockName.equals(stack.getNbt().getString("ore"))) {
                            outputValuableBlockPos(player, position, blockName);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);

            if(Screen.hasControlDown()) {
                setRadius(stack);
                user.sendMessage(new LiteralText(new TranslatableText("item.orescanner.scanner.radius").getString() + getRadius(stack)), false);
            } else if(Screen.hasShiftDown()) {
                if(stack.getNbt().contains("ore")) {
                    stack.removeSubNbt("ore");
                    user.sendMessage(new TranslatableText("item.orescanner.scanner.clear_ore"), false);
                }
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    public int getRadius(ItemStack stack) {
        if (stack.hasNbt()) {
            if (stack.getNbt().contains("radius")) {
                return stack.getNbt().getInt("radius");
            } else {
                return min_radius;
            }
        } else {
            return min_radius;
        }
    }

    public void setRadius(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.getOrCreateNbt().putInt("radius", min_radius + 1);
        } else {
            if (stack.getNbt().contains("radius")) {
                int radius = stack.getNbt().getInt("radius");
                if (radius >= min_radius && radius < max_radius) {
                    radius++;
                } else {
                    radius = min_radius;
                }
                stack.getOrCreateNbt().putInt("radius", radius);
            } else {
                stack.getOrCreateNbt().putInt("radius", min_radius + 1);
            }
        }
    }

    public long getEnergyCost(ItemStack stack) {
        float r = getRadius(stack);
        float e = energyCost;
        return Math.round(e/2.0 + (r/min_radius)*(r/min_radius)*(e/2.0));
    }

    private String countOres(ArrayList<String> blocks) {
        HashSet<String> distinctKeySet = new HashSet<>();
        HashMap<String, Integer> keyCountMap = new HashMap<>();

        for (String block : blocks) {
            if (distinctKeySet.add(block))
                keyCountMap.put(block, 1);
            else
                keyCountMap.put(block, (keyCountMap.get(block)) + 1);
        }

        ArrayList<String> hashToList = new ArrayList<>();
        for (Map.Entry<String, Integer> pair: keyCountMap.entrySet()) {
            hashToList.add(pair.getKey() + " - " + pair.getValue().toString());
        }
        return String.join(",\n", hashToList);
    }

    private void outputValuableBlocks(PlayerEntity player, String blocks) {
        player.sendMessage(new LiteralText(new TranslatableText("item.orescanner.scanner.found").getString() + "\n§a" + blocks), false);
    }

    private void outputValuableBlockPos(PlayerEntity player, BlockPos blockPos, String blockName) {
        player.sendMessage(new LiteralText(new TranslatableText("item.orescanner.scanner.found").getString() + "\n§a" + blockName + " (" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + ")"), false);
    }


    private boolean isOre(World world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();

        return !state.isAir()
                && !(block instanceof FluidBlock)
                && state.getHardness(world, blockPos) >= 0f
                && isOreCheckId(Registry.BLOCK.getId(block).toString());
    }

    boolean isOreCheckId(String id) {
        return id.endsWith("_ore") || additionalBlocks.contains(id);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        LiteralText line1 = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip_radius").getString() + getRadius(stack));
        LiteralText line2 = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip_shift").getString());
        LiteralText line_shift_1 = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip1").getString());
        LiteralText line_shift_2 = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip2").getString());
        LiteralText line_shift_3 = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip3").getString());
        LiteralText line_shift_4 = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip4").getString());

        tooltip.add(line1.formatted(Formatting.YELLOW));

        if (stack.hasNbt()) {
            if (stack.getNbt().contains("ore")) {
                LiteralText ore_text = new LiteralText(new TranslatableText("item.orescanner.scanner.tooltip_ore").getString() + stack.getNbt().getString("ore"));
                tooltip.add(ore_text.formatted(Formatting.BLUE));
            }
        }

        if(Screen.hasShiftDown()) {
            tooltip.add(line_shift_1.formatted(Formatting.GRAY));
            tooltip.add(line_shift_2.formatted(Formatting.GRAY));
            tooltip.add(line_shift_3.formatted(Formatting.GRAY));
            tooltip.add(line_shift_4.formatted(Formatting.GRAY));
        } else {
            tooltip.add(line2);
        }
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return ItemUtils.getPowerForDurabilityBar(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ItemUtils.getColorForDurabilityBar(stack);
    }


    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public long getEnergyCapacity() {
        return maxCharge;
    }

    @Override
    public long getEnergyMaxOutput() {
        return 0;
    }

    @Override
    public RcEnergyTier getTier() {
        return RcEnergyTier.MEDIUM;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (!isIn(group)) {
            return;
        }
        InitUtils.initPoweredItems(this, stacks);
    }
}
