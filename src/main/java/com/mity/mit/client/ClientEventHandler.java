package com.mity.mit.client;

import com.mity.mit.network.EatItemPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Слушатель событий на КЛИЕНТЕ.
 *
 * @EventBusSubscriber автоматически регистрирует все @SubscribeEvent методы.
 * Dist.CLIENT — класс загружается только на клиенте,
 * на сервере его вообще не существует.
 *
 * Используем NeoForge EVENT_BUS (не MOD bus) — это шина для
 * игровых событий (нажатия, тики, и т.д.).
 */
@EventBusSubscriber(modid = "eateverythingblock", value = Dist.CLIENT)
public class ClientEventHandler {

    /**
     * Срабатывает при каждом нажатии/отпускании любой клавиши.
     *
     * consumeClick() возвращает true если:
     *   1. Именно EAT_KEY была нажата
     *   2. Нажатие ещё не было "потреблено" (обработано)
     * После вызова consumeClick() нажатие считается обработанным.
     *
     * PacketDistributor.sendToServer() — отправляем пустой пакет-сигнал
     * серверу. Сервер знает от кого пришёл пакет, поэтому данных
     * передавать не нужно.
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.EAT_KEY.consumeClick()) {
            // Кнопка нажата — говорим серверу "съешь то что в руке"
            PacketDistributor.sendToServer(new EatItemPacket());
        }
    }
}