package io.github.projectet.ae2things.recipe;

import io.github.projectet.ae2things.AE2Things;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class CrystalGrowthRecipe implements Recipe<Inventory> {

    public final static Identifier TYPE_ID = AE2Things.id("crystal_growth_chamber");

    public final static RecipeType<CrystalGrowthRecipe> TYPE = RecipeType.register(TYPE_ID.toString());

    private final Identifier id;

    private final Ingredient flawlessCrystal;
    private final Ingredient flawedCrystal;
    private final Ingredient chippedCrystal;
    private final Ingredient damagedCrystal;
    private final ItemStack outputIngredient;

    public CrystalGrowthRecipe(Identifier id, Ingredient flawlessCrystal, Ingredient flawedCrystal,
                               Ingredient chippedCrystal, Ingredient damagedCrystal, ItemStack outputIngredient) {
        this.id = id;
        this.flawlessCrystal = flawlessCrystal;
        this.flawedCrystal = flawedCrystal;
        this.chippedCrystal = chippedCrystal;
        this.damagedCrystal = damagedCrystal;
        this.outputIngredient = outputIngredient;
    }

    public static Iterable<CrystalGrowthRecipe> getRecipes(World level) {
        return level.getRecipeManager().listAllOfType(CrystalGrowthRecipe.TYPE);
    }

    public static CrystalGrowthRecipe getRecipefromStack(World level, ItemStack item) {
        CrystalGrowthRecipe matchedRecipe = null;
        for (CrystalGrowthRecipe recipe : getRecipes(level)) {
            for(Ingredient ingredient : recipe.getIngredients()) {
                if(ingredient.test(item)) {
                    matchedRecipe = recipe;
                    break;
                }
            }
            if(matchedRecipe != null)
                break;
        }
        return matchedRecipe;
    }

    public boolean isFlawless(ItemStack testStack) {
        return getFlawlessCrystal().test(testStack);
    }

    public Ingredient getFlawlessCrystal() {
        return flawlessCrystal;
    }

    public Ingredient getFlawedCrystal() {
        return flawedCrystal;
    }

    public Ingredient getChippedCrystal() {
        return chippedCrystal;
    }

    public Ingredient getDamagedCrystal() {
        return damagedCrystal;
    }

    public Item nextStage(ItemStack item) {
        if(isFlawless(item))
            return Items.AIR;
        else if(getFlawedCrystal().test(item))
            return getChippedCrystal().isEmpty() ? Items.AIR : getChippedCrystal().getMatchingStacks()[0].getItem();
        else if(getChippedCrystal().test(item))
            return getDamagedCrystal().isEmpty() ? Items.AIR : getDamagedCrystal().getMatchingStacks()[0].getItem();
        return Items.AIR;
    }

    @Override
    public boolean matches(Inventory container, World level) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager dynamicRegistryManager) {
        return outputIngredient.copy();
    }


    @Override
    public boolean fits(int i, int j) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager dynamicRegistryManager) {
        return outputIngredient;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        ingredients.add(this.flawlessCrystal);
        ingredients.add(this.flawedCrystal);
        ingredients.add(this.chippedCrystal);
        ingredients.add(this.damagedCrystal);
        return ingredients;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CrystalGrowthRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
