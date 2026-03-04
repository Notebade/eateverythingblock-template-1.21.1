package com.mity.mit.items;

import com.mity.mit.eateverythingblock;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModeItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(eateverythingblock.MODID); // тут регистрируем предметы

    public static final DeferredItem<Item> GOUDA = ITEMS.register(
            "gouda",
            () -> new Item(new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
