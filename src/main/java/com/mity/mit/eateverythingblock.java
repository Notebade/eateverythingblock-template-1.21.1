package com.mity.mit;

import com.mity.mit.items.ModeItem;
import com.mity.mit.network.EatItemPacket;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(eateverythingblock.MODID)
public class eateverythingblock {

    // -------------------------------------------------------------------------
    // Константы мода
    // -------------------------------------------------------------------------

    /** ID мода — должен совпадать с META-INF/neoforge.mods.toml */
    public static final String MODID = "eateverythingblock";

    /** Логгер — используем для отладки через LOGGER.info() */
    public static final Logger LOGGER = LogUtils.getLogger();

    // -------------------------------------------------------------------------
    // Deferred Register — отложенная регистрация объектов.
    // NeoForge сам вызовет регистрацию в нужный момент загрузки.
    // -------------------------------------------------------------------------

    /** Реестр блоков мода */
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MODID);

    /** Реестр предметов мода */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    /** Реестр вкладок творческого режима */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    /**
     * Тестовый предмет-еда "eateverythingblock:example_item".
     * nutrition(1) — восстанавливает 1 единицу голода.
     * saturationModifier(2f) — коэффициент насыщения.
     * alwaysEdible() — можно есть даже с полным голодом.
     */
/*    public static final DeferredItem<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .alwaysEdible()
                            .nutrition(1)
                            .saturationModifier(2f)
                            .build()));*/

    /**
     * Вкладка творческого режима "eateverythingblock:example_tab".
     * Размещается перед вкладкой Combat.
     * Иконка вкладки — example_item.
     */
/*    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.eateverythingblock"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(EXAMPLE_ITEM.get());
                    }).build());*/

    // -------------------------------------------------------------------------
    // Конструктор — точка входа мода, вызывается один раз при загрузке
    // -------------------------------------------------------------------------

    /**
     * FML автоматически передаёт IEventBus и ModContainer при создании.
     * Здесь подключаем все регистраторы и слушатели событий.
     */
    public eateverythingblock(IEventBus modEventBus, ModContainer modContainer) {

        // Слушаем событие общей инициализации (commonSetup)
        modEventBus.addListener(this::commonSetup);

        // Подключаем все Deferred Register к шине мода —
        // они сами зарегистрируют объекты в нужный момент
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Регистрируем предметы из ModeItem
        ModeItem.register(modEventBus);

        // Регистрируем сетевые пакеты мода
        modEventBus.addListener(this::registerPayloads);

        // Регистрируем этот класс на игровой шине событий (NeoForge.EVENT_BUS).
        // Нужно только если в этом классе есть @SubscribeEvent методы.
        NeoForge.EVENT_BUS.register(this);

        // Слушаем событие заполнения вкладок творческого режима
        modEventBus.addListener(this::addCreative);

        // Регистрируем файл конфига мода
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // -------------------------------------------------------------------------
    // Методы инициализации
    // -------------------------------------------------------------------------

    /**
     * Общая инициализация — вызывается после регистрации всех объектов.
     * Используй для настройки вещей которые зависят от других модов.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        // Здесь пока ничего — будет нужно в следующих итерациях
    }

    /**
     * Регистрируем все сетевые пакеты мода.
     *
     * playToServer — пакет идёт только от клиента к серверу.
     * "1" — версия протокола (для совместимости при обновлении мода).
     */
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                EatItemPacket.TYPE,         // уникальный ID пакета
                EatItemPacket.STREAM_CODEC, // сериализация/десериализация
                EatItemPacket::handle       // обработчик на сервере
        );
    }

    /**
     * Добавляем предметы в ванильные вкладки творческого режима.
     * Вызывается для каждой вкладки отдельно.
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // предмет ГОЙДА из ModeItem → вкладка "Ингредиенты"
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModeItem.GOUDA);
        }
    }

    // -------------------------------------------------------------------------
    // Игровые события (NeoForge.EVENT_BUS)
    // -------------------------------------------------------------------------

    /** Вызывается когда сервер запускается — удобно для отладки */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("eateverythingblock: сервер запущен");
    }
}