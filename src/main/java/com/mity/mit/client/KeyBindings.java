package com.mity.mit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Регистрация кнопок управления мода.
 * Здесь только ОБЪЯВЛЕНИЕ кнопок — они появятся в настройках
 * управления Minecraft в разделе "eateverythingblock".
 * Игрок может переназначить кнопку на любую другую.
 */
public class KeyBindings {

    /**
     * Кнопка "Съесть предмет в руке".
     *
     * Параметры KeyMapping:
     *   1. translation key — ключ для перевода названия кнопки (en_us.json)
     *   2. KeyConflictContext — когда кнопка активна (IN_GAME = только в игре,
     *      не в меню)
     *   3. InputConstants.Type.KEYSYM — тип ввода (клавиатура, не мышь)
     *   4. GLFW_KEY_G — кнопка G по умолчанию
     *   5. категория в настройках управления
     */
    public static final KeyMapping EAT_KEY = new KeyMapping(
            "key.eateverythingblock.eat",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.eateverythingblock"
    );
}