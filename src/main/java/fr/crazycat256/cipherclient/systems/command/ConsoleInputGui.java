/*
 * This file is part of CipherClient (https://github.com/crazycat256/CipherClient).
 * Copyright (c) crazycat256.
 */

package fr.crazycat256.cipherclient.systems.command;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.crazycat256.cipherclient.CipherClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tv.twitch.chat.ChatUserInfo;

@SideOnly(Side.CLIENT)
public class ConsoleInputGui extends GuiScreen implements GuiYesNoCallback {

    private static final Set field_152175_f = Sets.newHashSet("http", "https");
    private static final Logger logger = LogManager.getLogger();
    private String field_146410_g = "";
    /**
     * keeps position of which chat message you will select when you press up,
     * (does not increase for duplicated messages sent immediately after each
     * other)
     */
    private int sentHistoryCursor = -1;
    private boolean field_146417_i;
    private boolean field_146414_r;
    private int field_146413_s;
    private final List<String> field_146412_t = new ArrayList<>();
    /**
     * used to pass around the URI to various dialogues and to the host os
     */
    private URI clickedURI;
    /**
     * Chat entry field
     */
    protected GuiTextField inputField;
    /**
     * is the text that appears when you press the chat key and the input box
     * appears pre-filled
     */
    private String defaultInputFieldText = "";
    private static final String __OBFID = "CL_00000682";

    public ConsoleInputGui() {
    }

    public ConsoleInputGui(String p_i1024_1_) {
        this.defaultInputFieldText = p_i1024_1_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = CipherClient.consoleGui.getSentMessages().size();
        this.inputField = new GuiTextField(this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(100);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat
     * events
     */
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        CipherClient.consoleGui.resetScroll();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        this.inputField.updateCursorCounter();
    }

    /**
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!CipherClient.clickGui.canInputConsole) {
            return;
        }
        this.field_146414_r = false;

        if (keyCode == 15) {
            this.getTabComplete();
        } else {
            this.field_146417_i = false;
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        } else if (keyCode != 28 && keyCode != 156) {
            switch (keyCode) {
                case 200:
                    this.getSentHistory(-1);
                    break;
                case 208:
                    this.getSentHistory(1);
                    break;
                case 201:
                    CipherClient.consoleGui.scroll(CipherClient.consoleGui.getLineCount() - 1);
                    break;
                case 209:
                    CipherClient.consoleGui.scroll(-CipherClient.consoleGui.getLineCount() + 1);
                    break;
                default:
                    this.inputField.textboxKeyTyped(typedChar, keyCode);
                    break;
            }
        } else {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty()) {
                this.sendMessage(s);
            }

            this.mc.displayGuiScreen(null);
        }
    }

    public void sendMessage(String message) {
        CommandProcessor.processString(message);
        CipherClient.consoleGui.addToSentMessages(message);
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0) {
            if (i > 1) {
                i = 1;
            }

            if (i < -1) {
                i = -1;
            }

            if (!isShiftKeyDown()) {
                i *= 7;
            }

            CipherClient.consoleGui.scroll(i);
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.mc.gameSettings.chatLinks) {
            IChatComponent ichatcomponent = CipherClient.consoleGui.getChatComponent(Mouse.getX(), Mouse.getY());

            if (ichatcomponent != null) {
                ClickEvent clickevent = ichatcomponent.getChatStyle().getChatClickEvent();

                if (clickevent != null) {
                    if (isShiftKeyDown()) {
                        this.inputField.writeText(ichatcomponent.getUnformattedTextForChat());
                    } else {
                        URI uri;

                        if (null == clickevent.getAction()) {
                            logger.error("Don't know how to handle " + clickevent);
                        } else {
                            switch (clickevent.getAction()) {
                                case OPEN_URL:
                                    try {
                                        uri = new URI(clickevent.getValue());

                                        if (!field_152175_f.contains(uri.getScheme().toLowerCase())) {
                                            throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + uri.getScheme().toLowerCase());
                                        }

                                        if (this.mc.gameSettings.chatLinksPrompt) {
                                            this.clickedURI = uri;
                                            this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.getValue(), 0, false));
                                        } else {
                                            this.openLink(uri);
                                        }
                                    } catch (URISyntaxException urisyntaxexception) {
                                        logger.error("Can't open url for " + clickevent, urisyntaxexception);
                                    }
                                    break;
                                case OPEN_FILE:
                                    uri = (new File(clickevent.getValue())).toURI();
                                    this.openLink(uri);
                                    break;
                                case SUGGEST_COMMAND:
                                    this.inputField.setText(clickevent.getValue());
                                    break;
                                case RUN_COMMAND:
                                    this.sendMessage(clickevent.getValue());
                                    break;
                                case TWITCH_USER_INFO:
                                    ChatUserInfo chatuserinfo = this.mc.func_152346_Z().func_152926_a(clickevent.getValue());
                                    if (chatuserinfo != null) {
                                        this.mc.displayGuiScreen(new GuiTwitchUserMode(this.mc.func_152346_Z(), chatuserinfo));
                                    } else {
                                        logger.error("Tried to handle twitch user but couldn't find them!");
                                    }
                                    break;
                                default:
                                    logger.error("Don't know how to handle " + clickevent);
                                    break;
                            }
                        }
                    }

                    return;
                }
            }
        }

        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void confirmClicked(boolean p_73878_1_, int p_73878_2_) {
        if (p_73878_2_ == 0) {
            if (p_73878_1_) {
                this.openLink(this.clickedURI);
            }

            this.clickedURI = null;
            this.mc.displayGuiScreen(this);
        }
    }

    private void openLink(URI link) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke(null);
            oclass.getMethod("browse", URI.class).invoke(object, link);
        } catch (Throwable throwable) {
            logger.error("Couldn't open link", throwable);
        }
    }

    public void getTabComplete() {
        String s1;

        if (this.field_146417_i) {
            this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());

            if (this.field_146413_s >= this.field_146412_t.size()) {
                this.field_146413_s = 0;
            }
        } else {
            int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.field_146412_t.clear();
            this.field_146413_s = 0;
            String s = this.inputField.getText().substring(i).toLowerCase();
            s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
            this.getAutoComplete(s1, s);

            if (this.field_146412_t.isEmpty()) {
                return;
            }

            this.field_146417_i = true;
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
        }

        if (this.field_146412_t.size() > 1) {
            StringBuilder stringbuilder = new StringBuilder();

            for (Iterator<?> iterator = this.field_146412_t.iterator(); iterator.hasNext(); stringbuilder.append(s1)) {
                s1 = (String) iterator.next();

                if (stringbuilder.length() > 0) {
                    stringbuilder.append(", ");
                }
            }

            CipherClient.consoleGui.printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }

        this.inputField.writeText(EnumChatFormatting.getTextWithoutFormattingCodes(this.field_146412_t.get(this.field_146413_s++)));
    }

    private void getAutoComplete(String p_146405_1_, String p_146405_2_) {
        if (!p_146405_1_.isEmpty()) {
            this.field_146414_r = true;
            this.func_146406_a(CommandProcessor.autoComplete(p_146405_1_));
        }
    }

    /**
     * input is relative and is applied directly to the sentHistoryCursor so -1
     * is the previous message, 1 is the next message from the current cursor
     * position
     */
    public void getSentHistory(int p_146402_1_) {
        int j = this.sentHistoryCursor + p_146402_1_;
        int k = CipherClient.consoleGui.getSentMessages().size();

        if (j < 0) {
            j = 0;
        }

        if (j > k) {
            j = k;
        }

        if (j != this.sentHistoryCursor) {
            if (j == k) {
                this.sentHistoryCursor = k;
                this.inputField.setText(this.field_146410_g);
            } else {
                if (this.sentHistoryCursor == k) {
                    this.field_146410_g = this.inputField.getText();
                }

                this.inputField.setText(CipherClient.consoleGui.getSentMessages().get(j));
                this.sentHistoryCursor = j;
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        if (this.inputField.getText().isEmpty()) {
            this.inputField.setText("/");
        }
        if (this.inputField.getText().charAt(0) != '/') {
            this.inputField.setText("/" + this.inputField.getText());
        }
        this.inputField.drawTextBox();
        IChatComponent ichatcomponent = CipherClient.consoleGui.getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
            HoverEvent hoverevent = ichatcomponent.getChatStyle().getChatHoverEvent();

            if (null != hoverevent.getAction()) {
                switch (hoverevent.getAction()) {
                    case SHOW_ITEM:
                        ItemStack itemstack = null;
                        try {
                            NBTBase nbtbase = JsonToNBT.func_150315_a(hoverevent.getValue().getUnformattedText());

                            if (nbtbase instanceof NBTTagCompound) {
                                itemstack = ItemStack.loadItemStackFromNBT((NBTTagCompound) nbtbase);
                            }
                        } catch (NBTException ignored) {
                        }
                        if (itemstack != null) {
                            this.renderToolTip(itemstack, p_73863_1_, p_73863_2_);
                        } else {
                            this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Item!", p_73863_1_, p_73863_2_);
                        }
                        break;
                    case SHOW_TEXT:
                        this.func_146283_a(Splitter.on("\n").splitToList(hoverevent.getValue().getFormattedText()), p_73863_1_, p_73863_2_);
                        break;
                    case SHOW_ACHIEVEMENT:
                        StatBase statbase = StatList.func_151177_a(hoverevent.getValue().getUnformattedText());
                        if (statbase != null) {
                            IChatComponent ichatcomponent1 = statbase.func_150951_e();
                            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("stats.tooltip.type." + (statbase.isAchievement() ? "achievement" : "statistic"));
                            chatcomponenttranslation.getChatStyle().setItalic(Boolean.TRUE);
                            String s = statbase instanceof Achievement ? ((Achievement) statbase).getDescription() : null;
                            ArrayList<String> arraylist = Lists.newArrayList(ichatcomponent1.getFormattedText(), chatcomponenttranslation.getFormattedText());

                            if (s != null) {
                                arraylist.addAll(this.fontRendererObj.listFormattedStringToWidth(s, 150));
                            }

                            this.func_146283_a(arraylist, p_73863_1_, p_73863_2_);
                        } else {
                            this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid statistic/achievement!", p_73863_1_, p_73863_2_);
                        }
                        break;
                    default:
                        break;
                }
            }

            GL11.glDisable(GL11.GL_LIGHTING);
        }

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    public void func_146406_a(String[] p_146406_1_) {
        if (this.field_146414_r) {
            this.field_146417_i = false;
            this.field_146412_t.clear();

            for (String s : p_146406_1_) {
                if (!s.isEmpty()) {
                    this.field_146412_t.add(s);
                }
            }

            String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(p_146406_1_);

            if (!s2.isEmpty() && !s1.equalsIgnoreCase(s2)) {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(s2);
            } else if (!this.field_146412_t.isEmpty()) {
                this.field_146417_i = true;
                this.getTabComplete();
            }
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in
     * single-player
     */
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
