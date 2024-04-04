package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class CrystalGrowthRootPanel extends UpgradeableScreen<CrystalGrowthMenu> {

    private final ProgressBar pb1;
    private final ProgressBar pb2;
    private final ProgressBar pb3;

    public CrystalGrowthRootPanel(CrystalGrowthMenu menu, PlayerInventory playerInventory, Text title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb1 = new ProgressBar(this.handler.topRow, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        this.pb2 = new ProgressBar(this.handler.midRow, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        this.pb3 = new ProgressBar(this.handler.botRow, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("pb1", this.pb1);
        widgets.add("pb2", this.pb2);
        widgets.add("pb3", this.pb3);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.pb1.setFullMsg(Text.literal((this.handler.topRow.getCurrentProgress() * 100 / this.handler.topRow.getMaxProgress()) + "%"));
        this.pb2.setFullMsg(Text.literal((this.handler.midRow.getCurrentProgress() * 100 / this.handler.midRow.getMaxProgress()) + "%"));
        this.pb3.setFullMsg(Text.literal((this.handler.botRow.getCurrentProgress() * 100 / this.handler.botRow.getMaxProgress()) + "%"));
    }
}
