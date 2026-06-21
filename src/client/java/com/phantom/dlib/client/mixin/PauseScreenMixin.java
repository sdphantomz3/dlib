package com.phantom.dlib.client.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.phantom.dlib.client.gui.DLibMainScreen;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void dlib$addConfigButton(CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        // 1. Load the gear assets textures
        WidgetSprites gearSprites = new WidgetSprites(
            Identifier.fromNamespaceAndPath("dlib", "config"),
            Identifier.fromNamespaceAndPath("dlib", "config-highlighted")
        );

        AbstractWidget targetButton = null;
        boolean isSingleplayer = this.minecraft.hasSingleplayerServer();

        // 2. Identify target button label depending on world type environment
        String targetLabel = isSingleplayer 
            ? Component.translatable("menu.multiplayerOptions.button").getString()
            : Component.translatable("menu.options").getString();

        // 3. Scan generated grid layout children to extract actual calculated placement
        for (var element : this.children()) {
            if (element instanceof AbstractWidget widget) {
                if (widget.getMessage().getString().equals(targetLabel)) {
                    targetButton = widget;
                    break;
                }
            }
        }

        // 4. Perform localized layout compression adjustments
        if (targetButton != null) {
            int originalX = targetButton.getX();
            int originalY = targetButton.getY();

            if (isSingleplayer) {
                // Shrink standard right-hand button (98 -> 74)
                targetButton.setWidth(74);
                
                // Math: start at original x position + new 74px width + 4px spacing gap
                int gearX = originalX + 74 + 4;
                
                this.addRenderableWidget(new ImageButton(gearX, originalY, 20, 20, gearSprites, (button) -> {
                    this.minecraft.gui.setScreen(new DLibMainScreen(this));
                }));
            } else {
                // Shrink standard wide button row layout (204 -> 180)
                targetButton.setWidth(180);
                
                // Math: start at original x position + new 180px width + 4px spacing gap
                int gearX = originalX + 180 + 4;
                
                this.addRenderableWidget(new ImageButton(gearX, originalY, 20, 20, gearSprites, (button) -> {
                    this.minecraft.gui.setScreen(new DLibMainScreen(this));
                }));
            }
        }
    }
}