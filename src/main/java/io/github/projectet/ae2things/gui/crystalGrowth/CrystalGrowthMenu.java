package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;
import io.github.projectet.ae2things.recipe.CrystalGrowthRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.world.World;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static ScreenHandlerType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT = MenuTypeBuilder.create(CrystalGrowthMenu::new, BECrystalGrowth.class).build("crystal_growth");

    private final InternalInventory inventory;
    private final World world;

    @GuiSync(2)
    public int topRowProg = 0;
    @GuiSync(3)
    public int midRowProg = 0;
    @GuiSync(4)
    public int botRowProg = 0;

    public final IProgressProvider topRow = new IProgressProvider() {
        @Override
        public int getCurrentProgress() {
            return topRowProg;
        }

        @Override
        public int getMaxProgress() {
            return 100;
        }
    };
    public final IProgressProvider midRow = new IProgressProvider() {
        @Override
        public int getCurrentProgress() {
            return midRowProg;
        }

        @Override
        public int getMaxProgress() {
            return 100;
        }
    };
    public final IProgressProvider botRow = new IProgressProvider() {
        @Override
        public int getCurrentProgress() {
            return botRowProg;
        }

        @Override
        public int getMaxProgress() {
            return 100;
        }
    };

    public CrystalGrowthMenu(int id, PlayerInventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);
        world = ip.player.getWorld();
        inventory = crystalGrowth.getInternalInventory();
        int i = 0;
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 4; x++) {
                int xx = 26 + (x * 36);
                int yy = 17 + (y * 18);
                CrystalGrowthSlot slot = new CrystalGrowthSlot(inventory, i, xx, yy, x, world);
                this.addSlot(slot, AE2Things.CG_SEMANTIC);
                i++;
            }
        }
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if(isServerSide()) {
            topRowProg = getHost().getTopProg();
            midRowProg = getHost().getMidProg();
            botRowProg = getHost().getBotProg();
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public boolean canUse(PlayerEntity PlayerEntity) {
        return true;
    }
}
