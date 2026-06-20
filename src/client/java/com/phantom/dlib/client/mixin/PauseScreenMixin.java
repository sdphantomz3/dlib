package com.phantom.dlib.client.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
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
        // Defines the textures for your gear icon (requires assets in your resources folder)
        WidgetSprites gearSprites = new WidgetSprites(
            Identifier.fromNamespaceAndPath("dlib", "textures/gui/sprites/config"),
            Identifier.fromNamespaceAndPath("dlib", "textures/gui/sprites/config-highlighted")
        );

        // Aligns the button to the right side of the center buttons
        int x = this.width / 2 + 104;
        int y = this.height / 4 + 72 + 12;

        this.addRenderableWidget(new ImageButton(x, y, 20, 20, gearSprites, (button) -> {
            if (this.minecraft != null) {
                // Minecraft 26.2 specific screen handling
                this.minecraft.gui.setScreen(new DLibMainScreen(this));
            }
        }));
    }
}