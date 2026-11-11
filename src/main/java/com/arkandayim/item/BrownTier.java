package com.arkandayim.item;

import com.arkandayim.registry.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class BrownTier implements Tier {

    @Override
    public int getUses() {
        return 2600;
    }

    @Override
    public float getSpeed() {
        return 9.5F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5.0F;
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.BROWN_INGOT.get());
    }
}
