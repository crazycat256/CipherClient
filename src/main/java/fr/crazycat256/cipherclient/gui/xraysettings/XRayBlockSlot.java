/*
 * This file is part of CipherClient (https://github.com/crazycat256/CipherClient).
 * Copyright (c) crazycat256.
 */

package fr.crazycat256.cipherclient.gui.xraysettings;

import fr.crazycat256.cipherclient.systems.module.Modules;
import fr.crazycat256.cipherclient.systems.module.render.xray.XRay;
import fr.crazycat256.cipherclient.systems.module.render.xray.XRayBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

public class XRayBlockSlot extends GuiSlot {

    int selectedIndex = -1;
    private final XRayGui xrayGui;

    public XRayBlockSlot(Minecraft par1Minecraft, int width, int height, int top, int bottom, int slotHeight, XRayGui xrayGui) {
        super(par1Minecraft, width, height, top, bottom, slotHeight);
        this.xrayGui = xrayGui;
    }

    @Override
    protected int getSize() {
        return Modules.get().get(XRay.class).blocks.size();
    }

    @Override
    protected boolean isSelected(int i) {
        return i == this.selectedIndex;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void elementClicked(int i, boolean var2, int var3, int var4) {
        this.selectedIndex = i;
    }

    @Override
    protected void drawSlot(int i, int j, int k, int var4, Tessellator var5, int var6, int var7) {
        XRayBlock xblock = Modules.get().get(XRay.class).blocks.get(i);
        XRayGui var10000 = this.xrayGui;
        XRayGui.drawRect((175 + j), (1 + k), (this.xrayGui.width - j - 20), (15 + k), (((51200 | xblock.r) << 8 | xblock.g) << 8 | xblock.b));
        if (xblock.id != null && Block.blockRegistry.containsKey(xblock.id)) {
            this.xrayGui.drawString(this.xrayGui.render, ((Block) Block.blockRegistry.getObject(xblock.id)).getLocalizedName(), j + 2, k + 1, 16777215);
        }
    }
}
