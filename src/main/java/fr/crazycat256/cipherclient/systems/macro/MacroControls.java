/*
 * This file is part of CipherClient (https://github.com/crazycat256/CipherClient).
 * Copyright (c) crazycat256.
 */

package fr.crazycat256.cipherclient.systems.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class MacroControls extends GuiScreen {

    /**
     * A reference to the screen object that created this. Used for navigating
     * between screens.
     */
    public GuiScreen parentScreen;
    protected String screenTitle;

    /**
     * The ID of the button that has been pressed.
     */
    public Macro currentKeyBinding = null;
    public long time;
    private GuiMacroList keyBindingList;

    public MacroControls(GuiScreen parentScreen) {
        super();
        this.parentScreen = parentScreen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        this.keyBindingList = new GuiMacroList(this, this.mc);
        this.buttonList.add(new GuiButton(200, this.width / 2 - 155, this.height - 29, 150, 20, "Done"));
        this.buttonList.add(new GuiButton(201, this.width / 2 - 155 + 160, this.height - 29, 150, 20, "Add"));
        this.screenTitle = "CipherClient macros";
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) {
            this.mc.displayGuiScreen(this.parentScreen);
            Macros.get().save();
        } else if (button.id == 201) {
            this.mc.displayGuiScreen(new GuiAddMacro(this));
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 || !this.keyBindingList.func_148179_a(mouseX, mouseY, mouseButton)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called when a mouse button is released. Args : mouseX, mouseY,
     * releaseButton\n \n@param state Will be negative to indicate mouse move
     * and will be either 0 or 1 to indicate mouse up.
     */
    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (state != 0 || !this.keyBindingList.func_148181_b(mouseX, mouseY, state)) {
            super.mouseMovedOrUp(mouseX, mouseY, state);
        }
    }

    public void setKeyBinding(Macro key, int keyCode) {
        key.setKeyCode(keyCode);
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is
     * the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character
     * (character on the key), keyCode (lwjgl Keyboard key code)
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.currentKeyBinding != null) {
            if (keyCode == 1) {
                setKeyBinding(this.currentKeyBinding, 0);
            } else {
                setKeyBinding(this.currentKeyBinding, keyCode);
            }

            this.currentKeyBinding = null;
            this.time = Minecraft.getSystemTime();
        } else {
            super.keyTyped(typedChar, keyCode);
            if (keyCode == 1) {
                mc.displayGuiScreen(parentScreen);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY,
     * renderPartialTicks
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.keyBindingList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
