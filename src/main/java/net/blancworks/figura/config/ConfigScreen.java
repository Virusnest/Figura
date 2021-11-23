package net.blancworks.figura.config;

import net.blancworks.figura.config.ConfigManager.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ConfigScreen extends Screen {

    public Screen parentScreen;
    private ConfigListWidget configListWidget;

    public ConfigScreen(Screen parentScreen) {
        super(new TranslatableText(ConfigManager.MOD_NAME + ".gui.config.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 29, 150, 20, new TranslatableText("gui.cancel"), (buttonWidgetx) -> {
            ConfigManager.discardConfig();
            this.client.openScreen(parentScreen);
        }));

        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 29, 150, 20, new TranslatableText("gui.done"), (buttonWidgetx) -> {
            ConfigManager.applyConfig();
            ConfigManager.saveConfig();
            this.client.openScreen(parentScreen);
        }));

        this.configListWidget = new ConfigListWidget(this, this.client);
        this.addChild(this.configListWidget);

        //generate configs...
        configListWidget.addEntries(Config.values());
    }

    @Override
    public void onClose() {
        ConfigManager.applyConfig();
        ConfigManager.saveConfig();
        this.client.openScreen(parentScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //background
        this.renderBackgroundTexture(0);

        //list
        this.configListWidget.render(matrices, mouseX, mouseY, delta);

        //buttons
        super.render(matrices, mouseX, mouseY, delta);

        //screen title
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 12, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (configListWidget.focusedBinding != null) {
            configListWidget.focusedBinding.setBoundKey(InputUtil.Type.MOUSE.createFromCode(button));
            configListWidget.focusedBinding = null;

            KeyBinding.updateKeysByCode();

            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (configListWidget.focusedBinding != null) {
            configListWidget.focusedBinding.setBoundKey(keyCode == 256 ? InputUtil.UNKNOWN_KEY: InputUtil.fromKeyCode(keyCode, scanCode));
            configListWidget.focusedBinding = null;

            KeyBinding.updateKeysByCode();

            return true;
        }
        else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}