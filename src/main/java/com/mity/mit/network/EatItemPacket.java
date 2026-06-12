package com.mity.mit.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Сетевой пакет — сигнал от клиента к серверу "игрок нажал кнопку съесть".
 *
 * Схема работы:
 *   [Клиент] Игрок нажимает G
 *       → ClientEventHandler ловит нажатие
 *       → отправляет EatItemPacket на сервер
 *   [Сервер] получает пакет
 *       → вызывает handle()
 *       → tryEat() применяет эффекты еды
 *
 * Используем record — пакет пустой, данных передавать не нужно,
 * просто сигнал "сделай действие для этого игрока".
 */
public record EatItemPacket() implements CustomPacketPayload {

    /**
     * Уникальный ID пакета — по нему NeoForge понимает какой это пакет.
     * Формат: "modid:название_пакета"
     */
    public static final CustomPacketPayload.Type<EatItemPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("eateverythingblock", "eat_item")
            );

    /**
     * Кодек для сериализации/десериализации пакета по сети.
     * StreamCodec.unit() — потому что пакет пустой, нечего кодировать.
     */
    public static final StreamCodec<FriendlyByteBuf, EatItemPacket> STREAM_CODEC =
            StreamCodec.unit(new EatItemPacket());

    /**
     * Обязательный метод интерфейса — возвращает TYPE этого пакета.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Вызывается на СЕРВЕРЕ когда пакет получен.
     * ctx.player() — это игрок который отправил пакет (тот кто нажал R).
     * enqueueWork() — выполняем в основном потоке сервера (thread-safe).
     */
    public static void handle(EatItemPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Кастуем к ServerPlayer — на сервере игрок всегда ServerPlayer
            if (ctx.player() instanceof ServerPlayer player) {
                tryEat(player);
            }
        });
    }

    /**
     * Логика для обычной еды
     *
     */
    private static void isFood(ServerPlayer player, ItemStack stack, FoodProperties food)
    {
        // Восстанавливаем голод (nutrition) и насыщение (saturation).
        // Это те же значения что использует ванильное поедание.
        player.getFoodData().eat(food.nutrition(), food.saturation());

        // Применяем все эффекты которые даёт эта еда.
        for (FoodProperties.PossibleEffect possibleEffect : food.effects()) {
            player.addEffect(new MobEffectInstance(possibleEffect.effect()));
        }

        // Уменьшаем количество предметов в стаке на 1
        // (как при обычном поедании)
        stack.shrink(1);

        // Воспроизводим звук поедания.
        // null вместо игрока — чтобы сам игрок тоже слышал звук.
        player.level().playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.GENERIC_EAT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,  // громкость
                1.0f   // высота тона
        );
    }

    /**
     * Логика для поедания не еды
     */
    private static void isNotFood(ServerPlayer player, ItemStack stack) {
        int nutrition = 0;
        float saturation = 0.0f;
        List<MobEffectInstance> effects = new ArrayList<>();
        playEatSound(player);
        if (player.level().random.nextFloat() < 0.05f) {
            effects.add(new MobEffectInstance(MobEffects.HUNGER, 300, 0)); // 15 секунд голода
        }

        if (stack.is(net.minecraft.world.item.Items.BEDROCK)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Ты попытался съесть бедрок... Это была плохая идея."));
            stack.shrink(1);
            player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
            return;
        }
        if (
                stack.is(ItemTags.LEAVES)
                        || stack.is(ItemTags.SAPLINGS)
                        || stack.is(ItemTags.FLOWERS)
                        || stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                        || stack.is(ItemTags.SMALL_FLOWERS)
                        || stack.is(ItemTags.TALL_FLOWERS)
        ) {
            nutrition = 3;
            saturation = 1.0f;
            if (player.level().random.nextFloat() < 0.8f) {
                effects.add(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
            }
        }
        else if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS) || stack.is(ItemTags.WOODEN_BUTTONS) || stack.is(ItemTags.WOODEN_DOORS)) {
            nutrition = 1;
            saturation = 0.5f;
        }
        else if (stack.is(ItemTags.DIRT) || stack.is(ItemTags.SAND)) {
            nutrition = 1;
            saturation = 0.2f;
        }
        // Если это руды, слитки, драгоценные камни (металлы и минералы)
        else if (stack.is(ItemTags.COALS) || isOreOrIngot(stack)) {
            nutrition = 4;
            saturation = 1.2f;
        }
        // Если это просто какой-то блок (например, стекло, шерсть, камень)
        else if (stack.getItem() instanceof BlockItem) {
            nutrition = 1;
            saturation = 0.1f;
        }
        else if (stack.getItem() instanceof AxeItem) {
            nutrition = 1;
            saturation = 0.3f;
            if (player.level().random.nextFloat() < 0.1f) {
                player.hurt(player.damageSources().playerAttack(player), 0.1f);
            }
            effects.add(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3000, 0));
        }
        else if (stack.getItem() instanceof PickaxeItem) {
            if (player.level().random.nextFloat() < 0.1f) {
                player.hurt(player.damageSources().playerAttack(player), 0.1f);
            }
            effects.add(new MobEffectInstance(MobEffects.DIG_SPEED, 2000, 2));
        }
        else if (stack.getItem() instanceof ShovelItem) {
            if (player.level().random.nextFloat() < 0.1f) {
                player.hurt(player.damageSources().playerAttack(player), 0.1f);
            }
        }
        else if (stack.getItem() instanceof SwordItem) {
            if (player.level().random.nextFloat() < 0.1f) {
                player.hurt(player.damageSources().playerAttack(player), 0.1f);
            }
            effects.add(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2));
        }
        else {
            nutrition = 1;
            saturation = 0.2f;
            if (player.level().random.nextFloat() < 0.1f) {
                effects.add(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            }
        }

        // 2. Применяем сгенерированные свойства
        player.getFoodData().eat(nutrition, saturation);
        for (MobEffectInstance effect : effects) {
            player.addEffect(effect);
        }
        stack.shrink(1);
    }

    // Вспомогательный метод для проверки руд и слитков (теги NeoForge/Forge)
    private static boolean isOreOrIngot(ItemStack stack) {
        // В NeoForge 1.21 теги могут быть в NeoForgeTags или ItemTags.
        // Для простоты проверим наличие слова "ore" или "ingot" в пути предмета,
        // либо используем стандартные теги, если они есть в твоей версии.
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        return id.getPath().contains("ore") || id.getPath().contains("ingot") || id.getPath().contains("gem");
    }

    private static void playEatSound(ServerPlayer player) {
        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }

    /**
     * Основная логика поедания — выполняется на сервере.
     * Сервер — авторитетный источник правды, поэтому всё делаем здесь,
     * а не на клиенте, чтобы нельзя было читерить.
     */
    private static void tryEat(ServerPlayer player) {
        // Берём предмет в правой руке
        ItemStack stack = player.getMainHandItem();

        // Если рука пустая — ничего не делаем
        if (stack.isEmpty()) return;

        // Получаем пищевые свойства предмета.
        // getFoodProperties() вернёт null если предмет не является едой.
        FoodProperties food = stack.getItem().getFoodProperties(stack, player);

        // Если предмет не еда — выходим (первая итерация, только обычная еда)
        if (food == null)  {
            isNotFood(player,stack);
            return;
        }

        isFood(player, stack, food);

    }
}