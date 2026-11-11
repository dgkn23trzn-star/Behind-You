package com.arkandayim.item;

import com.arkandayim.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The brown ingot set: heavy looking, dark leather-brown plates.
 */
public class BrownArmorMaterial implements ArmorMaterial {

    public static final BrownArmorMaterial INSTANCE = new BrownArmorMaterial();

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 176;
            case CHESTPLATE -> 256;
            case LEGGINGS -> 240;
            case BOOTS -> 208;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 3;
            case CHESTPLATE -> 7;
            case LEGGINGS -> 6;
            case BOOTS -> 3;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.BROWN_INGOT.get());
    }

    @Override
    public String getName() {
        return "brown";
    }

    @Override
    public float getToughness() {
        return 1.5F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.05F;
    }
}
