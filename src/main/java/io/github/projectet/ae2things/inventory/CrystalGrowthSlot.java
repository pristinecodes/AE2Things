package io.github.projectet.ae2things.inventory;

import appeng.api.inventories.InternalInventory;

import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import io.github.projectet.ae2things.recipe.CrystalGrowthRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class CrystalGrowthSlot extends Slot {

    private static final Inventory EMPTY_INVENTORY = new SimpleInventory(0);
    private final InternalInventory inventory;
    private final int invSlot;

    private World world;

    private AEBaseMenu menu = null;
    private boolean active = true;

    public final int slotIndex;


    public CrystalGrowthSlot(InternalInventory inv, int invSlot, int x, int y, int slotIndex, World world) {
        super(EMPTY_INVENTORY, invSlot, x, y);
        this.invSlot = invSlot;
        this.slotIndex = slotIndex;
        this.inventory = inv;
        this.world = world;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (containsWrapperItem()) {
            return false;
        }
        CrystalGrowthRecipe recipe = CrystalGrowthRecipe.getRecipefromStack(world, stack);
        if(recipe == null)
            return false;
        else {
            switch(slotIndex) {
                case 0 -> {
                    return recipe.getFlawlessCrystal().test(stack) || recipe.getFlawedCrystal().test(stack);
                }
                case 1 -> {
                    return recipe.getChippedCrystal().test(stack);
                }
                case 2 -> {
                    return recipe.getDamagedCrystal().test(stack);
                }
                default -> {
                    return false;
                }
            }
        }
    }

    @Override
    public ItemStack getStack() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }

        if (this.invSlot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }

        return this.inventory.getStackInSlot(this.invSlot);
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.inventory.setItemDirect(this.invSlot, stack);
            this.markDirty();
        }
    }

//    @Override
//    public void initialize(ItemStack stack) {
//        this.inventory.setItemDirect(this.invSlot, stack);
//    }

    private void notifyContainerSlotChanged() {
        if (this.getMenu() != null) {
            this.getMenu().onSlotChange(this);
        }
    }

    public InternalInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        notifyContainerSlotChanged();
    }

    @Override
    public int getMaxItemCount() {
        return this.inventory.getSlotLimit(this.invSlot);
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return Math.min(this.getMaxItemCount(), stack.getMaxCount());
    }

    @Override
    public boolean canTakeItems(PlayerEntity player) {
        if (containsWrapperItem()) {
            return false;
        }
        if (this.isSlotEnabled()) {
            return !this.inventory.extractItem(this.invSlot, 1, true).isEmpty();
        }
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (containsWrapperItem()) {
            return ItemStack.EMPTY;
        }

        return this.inventory.extractItem(this.invSlot, amount, false);
    }

    private boolean containsWrapperItem() {
        return GenericStack.isWrapped(getStack());
    }

    @Override
    public boolean isEnabled() {
        return this.isSlotEnabled() && active;
    }

    public boolean isSlotEnabled() {
        return true;
    }

    protected AEBaseMenu getMenu() {
        return this.menu;
    }

    public void setMenu(AEBaseMenu menu) {
        this.menu = menu;
    }
}
