package com.ultra.megamod.lib.accessories;

import com.google.common.reflect.Reflection;
import com.mojang.logging.LogUtils;
import com.ultra.megamod.lib.accessories.api.action.ActionResponse;
import com.ultra.megamod.lib.accessories.api.action.ActionResponseBuffer;
import com.ultra.megamod.lib.accessories.api.data.AccessoriesTags;
import com.ultra.megamod.lib.accessories.api.events.v2.AllowEntityModificationCallback;
import com.ultra.megamod.lib.accessories.api.tooltip.ComponentBuilder;
import com.ultra.megamod.lib.accessories.commands.AccessoriesCommands;
import com.ultra.megamod.lib.accessories.compat.config.AccessoriesConfig;
import com.ultra.megamod.lib.accessories.criteria.AccessoryChangedCriterion;
import com.ultra.megamod.lib.accessories.data.*;
import com.ultra.megamod.lib.accessories.impl.event.VanillaItemPredicates;
import com.ultra.megamod.lib.accessories.impl.option.AccessoriesPlayerOptionsHolder;
import com.ultra.megamod.lib.accessories.menu.AccessoriesMenuVariant;
import com.ultra.megamod.lib.accessories.menu.ArmorSlotTypes;
import com.ultra.megamod.mixin.accessories.CriteriaTriggersAccessor;
import com.ultra.megamod.lib.accessories.networking.AccessoriesNetworking;
import com.ultra.megamod.lib.accessories.networking.client.ScreenVariantPing;
import com.ultra.megamod.lib.accessories.networking.client.SyncServerOverrideOption;
import com.ultra.megamod.lib.accessories.utils.EndecUtils;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

public class Accessories {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Identifier SLOT_LOADER_LOCATION = Accessories.of("slot_loader");
    public static final Identifier ENTITY_SLOT_LOADER_LOCATION = Accessories.of("entity_slot_loader");
    public static final Identifier SLOT_GROUP_LOADER_LOCATION = Accessories.of("slot_group_loader");
    public static final Identifier DATA_RELOAD_HOOK = Accessories.of("data_reload_hook");

    public static final boolean DEBUG;

    static {
        boolean debug = AccessoriesLoaderInternals.INSTANCE.isDevelopmentEnv();

        if (System.getProperty("accessories.debug") != null) {
            debug = Boolean.getBoolean("accessories.debug");
        }


        DEBUG = debug;
    }

    public static final String MODID = "accessories";

    public static Identifier of(String path){
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static Identifier parseLocationOrDefault(String s){
        var location = Identifier.tryParse(s);

        if (location == null) location = Accessories.of(s);

        return location;
    }

    public static String translationKey(String ...path){
        return MODID + "." + String.join(".", path);
    }

    public static MutableComponent translation(String ...path) {
        return Component.translatable(translationKey(path));
    }

    public static ComponentBuilder translationWithArgs(String ...path) {
        return (args) -> Component.translatable(translationKey(path), args);
    }

    //--

    private static final AccessoriesConfig CONFIG = AccessoriesConfig.createAndLoad(serializationBuilder -> {
        serializationBuilder.addEndec(Vector2i.class, EndecUtils.VECTOR_2_I_ENDEC);
        serializationBuilder.addEndec(AccessoriesPlayerOptionsHolder.class, AccessoriesPlayerOptionsHolder.ENDEC);
    });

    public static AccessoriesConfig config(){
        return CONFIG;
    }

    //--

    public static void askPlayerForVariant(ServerPlayer player) {
        askPlayerForVariant(player, null);
    }

    public static void askPlayerForVariant(ServerPlayer player, @Nullable LivingEntity targetEntity) {
        AccessoriesNetworking.sendToPlayer(player, ScreenVariantPing.of(targetEntity));
    }

    public static boolean attemptOpenScreenPlayer(ServerPlayer player, AccessoriesMenuVariant variant) {
        var result = ProjectileUtil.getHitResultOnViewVector(player, e -> e instanceof LivingEntity, player.entityInteractionRange());

        if(!(result instanceof EntityHitResult entityHitResult)) return false;

        Accessories.openAccessoriesMenu(player, variant, (LivingEntity) entityHitResult.getEntity());

        return true;
    }

    public static void openAccessoriesMenu(Player player, AccessoriesMenuVariant variant, @Nullable LivingEntity targetEntity) {
        openAccessoriesMenu(player, variant, targetEntity, null);
    }

    public static void openAccessoriesMenu(Player player, AccessoriesMenuVariant variant, @Nullable LivingEntity targetEntity, @Nullable ItemStack carriedStack) {
        if(targetEntity != null && !player.equals(targetEntity)) {
            var buffer = new ActionResponseBuffer(false);

            AllowEntityModificationCallback.EVENT.invoker().allowModifications(targetEntity, player, null, buffer);

            // Permission check simplified - allow admins to bypass
            if(!buffer.canPerformAction().isValid(false)) return;
        }

        AccessoriesInternals.INSTANCE.openAccessoriesMenu(player, variant, targetEntity, carriedStack);
    }

    //--

    public static AccessoryChangedCriterion ACCESSORY_EQUIPPED;
    public static AccessoryChangedCriterion ACCESSORY_UNEQUIPPED;

    public static void init() {
        Reflection.initialize(SlotTypeLoader.class, SlotGroupLoader.class, EntitySlotLoader.class, CustomRendererLoader.class);

        AccessoriesCommands.init();

        AllowEntityModificationCallback.EVENT.register((target, player, reference, buffer) -> {
            var type = target.getType();

            if(type.is(AccessoriesTags.MODIFIABLE_ENTITY_BLACKLIST)) {
                buffer.respondWith(ActionResponse.of(false, Component.literal("Given entity can not be manged by you!")));
                return;
            }

            var isOwnersPet = (target instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null && ownableEntity.getOwner().equals(player));

            if(isOwnersPet || type.is(AccessoriesTags.MODIFIABLE_ENTITY_WHITELIST)) {
                buffer.respondWith(ActionResponse.of(true,
                    isOwnersPet
                        ? Component.literal("Your pet can be managed by you.")
                        : Component.literal("Given entity can be manged by you."))
                );
            }
        });

        ArmorSlotTypes.INSTANCE.init();

        VanillaItemPredicates.init();

        // TODO: remove when proper sync changes go into effect
        // Config sync hooks are disabled until OWO config wrapper is fully ported
        // These used SyncServerOverrideOption.hookUpdate which expects ConfigWrapper
    }

    public static void registerCriteria(){
        ACCESSORY_EQUIPPED = CriteriaTriggersAccessor.accessories$callRegister("accessories:equip_accessory", new AccessoryChangedCriterion());
        ACCESSORY_UNEQUIPPED = CriteriaTriggersAccessor.accessories$callRegister("accessories:unequip_accessory", new AccessoryChangedCriterion());
    }

    //--

    public static <T> T handleIoError(String dataName, Function<ProblemReporter.ScopedCollector, T> function) {
        return handleIoError(() -> dataName, function);
    }

    public static <T> T handleIoError(ProblemReporter.PathElement pathElement, Function<ProblemReporter.ScopedCollector, T> function) {
        try (var scopedCollector = new ProblemReporter.ScopedCollector(pathElement, Accessories.LOGGER)) {
            return function.apply(scopedCollector);
        }
    }

    public static void handleIoError(String dataName, Consumer<ProblemReporter.ScopedCollector> function) {
        handleIoError(() -> dataName, function);
    }

    public static void handleIoError(ProblemReporter.PathElement pathElement, Consumer<ProblemReporter.ScopedCollector> function) {
        try (var scopedCollector = new ProblemReporter.ScopedCollector(pathElement, Accessories.LOGGER)) {
            function.accept(scopedCollector);
        }
    }
}