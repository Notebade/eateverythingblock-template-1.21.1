package com.mity.mit;

import com.mity.mit.client.KeyBindings;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Точка входа мода на КЛИЕНТЕ.
 * Этот класс не загружается на выделенном сервере вообще —
 * поэтому здесь безопасно использовать клиентские классы Minecraft.
 *
 * Bus.MOD — используем шину мода (не игровую).
 * Шина мода используется для событий инициализации и регистрации.
 */
@Mod(value = eateverythingblock.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = eateverythingblock.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class eateverythingblockClient {

    /**
     * Конструктор вызывается один раз при загрузке мода на клиенте.
     * IEventBus modEventBus — шина событий мода, нужна для регистрации
     * кейбиндов (они регистрируются через событие на MOD bus).
     */
    public eateverythingblockClient(ModContainer container, IEventBus modEventBus) {
        // Подключаем экран конфига мода (Mods → твой мод → Config)
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Подписываемся на событие регистрации кнопок управления.
        // Важно делать через modEventBus, а не через @SubscribeEvent —
        // потому что RegisterKeyMappingsEvent это событие MOD bus.
        modEventBus.addListener(eateverythingblockClient::onRegisterKeyMappings);
    }

    /**
     * Вызывается во время инициализации клиента.
     * Хорошее место для настройки рендеринга, моделей и т.д.
     * Пока просто логируем для проверки что всё загрузилось.
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        eateverythingblock.LOGGER.info("HELLO FROM CLIENT SETUP");
        eateverythingblock.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    /**
     * Регистрируем нашу кнопку в системе управления Minecraft.
     * После этого она появится в Settings → Controls → eateverythingblock.
     */
    static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.EAT_KEY);
    }
}