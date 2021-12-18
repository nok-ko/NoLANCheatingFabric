// Copyright Nokko 2021
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
package me.nokko.nolancheating;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class NoLANCheating implements ClientModInitializer {


    /**
     * Check whether a widget contains text corresponding to a given translation key.
     * <p>Permissive: will return <code>true</code> even if the widget has the key mixed with other text.</p>
     *
     * @param widget The widget to check. Its <code>.getMessage()</code> should return a TranslatableText, but it won't crash if it
     *               doesn't.
     * @param key    A translation key
     * @return boolean value representing whether the given widget has a given translation key's translation
     * anywhere inside its translation, in the current language.
     */
    private static boolean widgetHasKey(ClickableWidget widget, String key) {
        // On the one hand, not a good way to do this, on the other hand, the proper way is a bit strange
        // (On the other other hand, this is called at most 7 times per screen init, it doesn't matter)
        return widget.getMessage().getString().contains(I18n.translate(key));
        // in the future, look into net.minecraft.text.TranslatableText#forEachPart ?
    }

    private final Logger logger = LogManager.getLogger();

    /**
     * Initialize the mod.
     * <p>Registers the GUI event.</p>
     * <p>We could have used a Mixin instead, but it's best to avoid them when possible, for other modders' sake.</p>
     * <p>Likewise, I could have just gotten the Nth button and called it a day, but if some other mod modified
     * the button order, it would be... not pleasant to figure that bug out. :P</p>
     */
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof OpenToLanScreen) {
                // Remove the "Allow Cheats" button
                logger.info(Screens.getButtons(screen).stream()
                        .map(widget -> widget.getMessage().getString())
                        .collect(Collectors.toList()));
                Screens.getButtons(screen).removeIf(widget -> widgetHasKey(widget, "selectWorld.allowCommands"));

                // Center the game mode button!
                try {
                    ClickableWidget gameModeButton = Screens.getButtons(screen)
                            .stream()
                            .filter(widget -> widgetHasKey(widget, "selectWorld.gameMode"))
                            .findFirst().orElseThrow();
                    gameModeButton.x = screen.width / 2 - 75;
                } catch (NoSuchElementException e) {
                    // Finding the game mode button can produce an exception:
                    // we should fail without crashing the entire game.
                    logger.error("WTF!?", e);
                }
            }
        });
    }
}
