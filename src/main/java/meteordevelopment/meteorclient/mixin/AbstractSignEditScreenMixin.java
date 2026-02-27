/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

/**
 * Mitigates the sign translation vulnerability (MC-265322) that allows servers to detect
 * installed mods by exploiting how sign text resolves translation keys and keybinds.
 *
 * Uses a whitelist approach: only vanilla Minecraft keys are allowed to resolve normally.
 * All other keys (from any mod) are converted to plain text literals to prevent detection.
 */
@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin {
    @ModifyExpressionValue(method = "<init>(Lnet/minecraft/block/entity/SignBlockEntity;ZZLnet/minecraft/text/Text;)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;mapToObj(Ljava/util/function/IntFunction;)Ljava/util/stream/Stream;"))
    private Stream<Text> modifyTranslatableText(Stream<Text> original) {
        return original.map(this::modifyText);
    }

    // based on https://github.com/JustAlittleWolf/ModDetectionPreventer
    @Unique
    private Text modifyText(Text message) {
        MutableText modified = MutableText.of(message.getContent());

        if (message.getContent() instanceof KeybindTextContent content) {
            String key = content.getKey();

            // Only allow vanilla keybinds (key.* that are part of vanilla Minecraft)
            if (!isVanillaKeybind(key)) {
                modified = MutableText.of(new PlainTextContent.Literal(key));
            }
        }
        if (message.getContent() instanceof TranslatableTextContent content) {
            String key = content.getKey();

            // Only allow vanilla translation keys
            if (!isVanillaTranslationKey(key)) {
                modified = MutableText.of(new PlainTextContent.Literal(key));
            }
        }

        modified.setStyle(message.getStyle());
        for (Text sibling : message.getSiblings()) {
            modified.append(modifyText(sibling));
        }

        return modified;
    }

    /**
     * Checks if a keybind key is a vanilla Minecraft keybind.
     * Vanilla keybinds use the format "key.keyboard.*", "key.mouse.*", or specific vanilla action keys.
     */
    @Unique
    private boolean isVanillaKeybind(String key) {
        // Vanilla keybind categories
        if (key.startsWith("key.keyboard.")) return true;
        if (key.startsWith("key.mouse.")) return true;

        // Vanilla action keybinds
        return switch (key) {
            case "key.attack", "key.use", "key.forward", "key.back", "key.left", "key.right",
                 "key.jump", "key.sneak", "key.sprint", "key.drop", "key.inventory",
                 "key.swapOffhand", "key.chat", "key.playerlist", "key.command",
                 "key.screenshot", "key.togglePerspective", "key.smoothCamera",
                 "key.fullscreen", "key.spectatorOutlines", "key.advancements",
                 "key.hotbar.1", "key.hotbar.2", "key.hotbar.3", "key.hotbar.4",
                 "key.hotbar.5", "key.hotbar.6", "key.hotbar.7", "key.hotbar.8",
                 "key.hotbar.9", "key.saveToolbarActivator", "key.loadToolbarActivator",
                 "key.pickItem", "key.socialInteractions" -> true;
            default -> false;
        };
    }

    /**
     * Checks if a translation key is a vanilla Minecraft translation key.
     * This uses a conservative whitelist of vanilla prefixes.
     */
    @Unique
    private boolean isVanillaTranslationKey(String key) {
        // Common vanilla translation key prefixes
        // These cover blocks, items, entities, GUI elements, etc.
        return key.startsWith("block.minecraft.") ||
            key.startsWith("item.minecraft.") ||
            key.startsWith("entity.minecraft.") ||
            key.startsWith("biome.minecraft.") ||
            key.startsWith("effect.minecraft.") ||
            key.startsWith("enchantment.minecraft.") ||
            key.startsWith("potion.minecraft.") ||
            key.startsWith("advancement.") ||
            key.startsWith("advancements.") ||
            key.startsWith("stat.minecraft.") ||
            key.startsWith("container.") ||
            key.startsWith("gui.") ||
            key.startsWith("menu.") ||
            key.startsWith("chat.") ||
            key.startsWith("commands.") ||
            key.startsWith("command.") ||
            key.startsWith("argument.") ||
            key.startsWith("selectWorld.") ||
            key.startsWith("createWorld.") ||
            key.startsWith("multiplayer.") ||
            key.startsWith("connect.") ||
            key.startsWith("disconnect.") ||
            key.startsWith("options.") ||
            key.startsWith("controls.") ||
            key.startsWith("key.") ||
            key.startsWith("soundCategory.") ||
            key.startsWith("record.") ||
            key.startsWith("subtitles.") ||
            key.startsWith("death.") ||
            key.startsWith("deathScreen.") ||
            key.startsWith("gameMode.") ||
            key.startsWith("selectServer.") ||
            key.startsWith("addServer.") ||
            key.startsWith("lanServer.") ||
            key.startsWith("title.") ||
            key.startsWith("narrator.") ||
            key.startsWith("accessibility.") ||
            key.startsWith("pack.") ||
            key.startsWith("resourcePack.") ||
            key.startsWith("dataPack.") ||
            key.startsWith("optimizeWorld.") ||
            key.startsWith("debug.") ||
            key.startsWith("demo.") ||
            key.startsWith("screenshot.") ||
            key.startsWith("book.") ||
            key.startsWith("lectern.") ||
            key.startsWith("merchant.") ||
            key.startsWith("filled_map.") ||
            key.startsWith("attribute.") ||
            key.startsWith("slot.") ||
            key.startsWith("color.") ||
            key.startsWith("painting.") ||
            key.startsWith("structure_block.") ||
            key.startsWith("jigsaw_block.") ||
            key.startsWith("gamerule.") ||
            key.startsWith("generator.") ||
            key.startsWith("flat_world_preset.") ||
            key.startsWith("world_preset.") ||
            key.startsWith("dimension.") ||
            key.startsWith("trim_material.") ||
            key.startsWith("trim_pattern.") ||
            key.startsWith("instrument.") ||
            key.startsWith("banner_pattern.") ||
            key.startsWith("wolf_variant.") ||
            key.startsWith("cat_variant.") ||
            key.startsWith("frog_variant.") ||
            key.startsWith("goat_horn_sound.") ||
            key.startsWith("spectatorMenu.") ||
            key.startsWith("telemetry.") ||
            key.startsWith("trial_spawner.");
    }
}
