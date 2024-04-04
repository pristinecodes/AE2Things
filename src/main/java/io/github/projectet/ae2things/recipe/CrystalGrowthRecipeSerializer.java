package io.github.projectet.ae2things.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.NotNull;

public class CrystalGrowthRecipeSerializer implements RecipeSerializer<CrystalGrowthRecipe> {

    public static final CrystalGrowthRecipeSerializer INSTANCE = new CrystalGrowthRecipeSerializer();

    private CrystalGrowthRecipeSerializer() {}

    @Override
    public CrystalGrowthRecipe read(@NotNull Identifier resourceLocation, @NotNull JsonObject jsonObject) {
        ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
        JsonObject ingredients = JsonHelper.getObject(jsonObject, "ingredients");
        Ingredient flawless = Ingredient.fromJson(ingredients.get("flawless"));
        Ingredient flawed = ingredients.has("flawed") ? Ingredient.fromJson(ingredients.get("flawed")) : Ingredient.EMPTY;
        Ingredient chipped = ingredients.has("chipped") ? Ingredient.fromJson(ingredients.get("chipped")) : Ingredient.EMPTY;
        Ingredient damaged = ingredients.has("damaged") ? Ingredient.fromJson(ingredients.get("damaged")) : Ingredient.EMPTY;
        return new CrystalGrowthRecipe(resourceLocation, flawless, flawed, chipped, damaged, output);
    }

    @Override
    public CrystalGrowthRecipe read(@NotNull Identifier resourceLocation, PacketByteBuf friendlyByteBuf) {
        ItemStack output = friendlyByteBuf.readItemStack();
        Ingredient flawless = Ingredient.fromPacket(friendlyByteBuf);
        Ingredient flawed = Ingredient.fromPacket(friendlyByteBuf);
        Ingredient chipped = Ingredient.fromPacket(friendlyByteBuf);
        Ingredient damaged = Ingredient.fromPacket(friendlyByteBuf);

        return new CrystalGrowthRecipe(resourceLocation, flawless, flawed, chipped, damaged, output);
    }

    @Override
    public void write(PacketByteBuf friendlyByteBuf, CrystalGrowthRecipe recipe) {
        friendlyByteBuf.writeItemStack(recipe.getOutput(null));
        recipe.getFlawlessCrystal().write(friendlyByteBuf);
        recipe.getFlawedCrystal().write(friendlyByteBuf);
        recipe.getChippedCrystal().write(friendlyByteBuf);
        recipe.getDamagedCrystal().write(friendlyByteBuf);
    }

}
