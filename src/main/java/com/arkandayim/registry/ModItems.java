package com.arkandayim.registry;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.entity.ModEntities;
import com.arkandayim.item.ArkandayimCompassItem;
import com.arkandayim.item.ArkandayimPanelItem;
import com.arkandayim.item.BlackBookItem;
import com.arkandayim.item.BrownArmorMaterial;
import com.arkandayim.item.BrownAxeItem;
import com.arkandayim.item.BrownHoeItem;
import com.arkandayim.item.BrownPickaxeItem;
import com.arkandayim.item.BrownShovelItem;
import com.arkandayim.item.BrownSwordItem;
import com.arkandayim.item.GuardianTotemItem;
import com.arkandayim.item.NullTotemItem;
import com.arkandayim.item.ShadowTotemItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ArkandayimMod.MODID);

    public static final RegistryObject<Item> ARAS_SPAWN_EGG = ITEMS.register("aras_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ARAS, 0x120608, 0xFF8A1E, new Item.Properties()));

    public static final RegistryObject<Item> BROWN_ORE = ITEMS.register("brown_ore",
            () -> new BlockItem(ModBlocks.BROWN_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> BROWN_INGOT = ITEMS.register("brown_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BROWN_SWORD = ITEMS.register("brown_sword",
            BrownSwordItem::new);

    public static final RegistryObject<Item> BROWN_PICKAXE = ITEMS.register("brown_pickaxe",
            BrownPickaxeItem::new);

    public static final RegistryObject<Item> BROWN_SHOVEL = ITEMS.register("brown_shovel",
            BrownShovelItem::new);

    public static final RegistryObject<Item> BROWN_AXE = ITEMS.register("brown_axe",
            BrownAxeItem::new);

    public static final RegistryObject<Item> BROWN_HOE = ITEMS.register("brown_hoe",
            BrownHoeItem::new);

    public static final RegistryObject<Item> ARKANDAYIM_PANEL = ITEMS.register("arkandayim_panel",
            () -> new ArkandayimPanelItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> BLACK_DIRT = ITEMS.register("black_dirt",
            () -> new BlockItem(ModBlocks.BLACK_DIRT.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLACK_STONE = ITEMS.register("black_stone",
            () -> new BlockItem(ModBlocks.BLACK_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLACK_PLANKS = ITEMS.register("black_planks",
            () -> new BlockItem(ModBlocks.BLACK_PLANKS.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLACK_LOG = ITEMS.register("black_log",
            () -> new BlockItem(ModBlocks.BLACK_LOG.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLACK_LEAVES = ITEMS.register("black_leaves",
            () -> new BlockItem(ModBlocks.BLACK_LEAVES.get(), new Item.Properties()));

    public static final RegistryObject<Item> DARK_STONE = ITEMS.register("dark_stone",
            () -> new BlockItem(ModBlocks.DARK_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> ASH_BLOCK = ITEMS.register("ash_block",
            () -> new BlockItem(ModBlocks.ASH_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> BROWN_HELMET = ITEMS.register("brown_helmet",
            () -> new ArmorItem(BrownArmorMaterial.INSTANCE, ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> BROWN_CHESTPLATE = ITEMS.register("brown_chestplate",
            () -> new ArmorItem(BrownArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> BROWN_LEGGINGS = ITEMS.register("brown_leggings",
            () -> new ArmorItem(BrownArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> BROWN_BOOTS = ITEMS.register("brown_boots",
            () -> new ArmorItem(BrownArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> ARKANDAYIM_COMPASS = ITEMS.register("arkandayim_compass",
            () -> new ArkandayimCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BLACK_BOOK = ITEMS.register("black_book",
            () -> new BlackBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> NULL_TOTEM = ITEMS.register("null_totem",
            () -> new NullTotemItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SHADOW_TOTEM = ITEMS.register("shadow_totem",
            () -> new ShadowTotemItem(new Item.Properties().stacksTo(4).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> GUARDIAN_TOTEM = ITEMS.register("guardian_totem",
            () -> new GuardianTotemItem(new Item.Properties().stacksTo(4).rarity(Rarity.EPIC)));
}
