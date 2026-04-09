package com.ultra.megamod.feature.citizen.visitor;

import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Simple NPC entity for tavern visitors.
 * Extends PathfinderMob (NOT MCEntityCitizen — visitors are temporary).
 * Sits at the tavern, is peaceful, and right-click opens a recruitment dialog.
 */
public class VisitorEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_VISITOR_ID =
            SynchedEntityData.defineId(VisitorEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_SITTING =
            SynchedEntityData.defineId(VisitorEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID visitorId;

    public VisitorEntity(EntityType<? extends VisitorEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createVisitorAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0) // Doesn't move — sits at tavern
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VISITOR_ID, "");
        builder.define(DATA_SITTING, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, (Goal) new RandomLookAroundGoal((Mob) this));
    }

    // --- Sitting behavior ---

    @Override
    public void aiStep() {
        super.aiStep();

        // Keep the entity sitting (frozen in place)
        if (isSitting()) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            this.setYRot(this.yRotO);
        }
    }

    public boolean isSitting() {
        return this.entityData.get(DATA_SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(DATA_SITTING, sitting);
    }

    // --- Visitor ID ---

    public UUID getVisitorId() {
        if (visitorId == null) {
            String s = this.entityData.get(DATA_VISITOR_ID);
            if (!s.isEmpty()) {
                try {
                    visitorId = UUID.fromString(s);
                } catch (IllegalArgumentException e) {
                    visitorId = null;
                }
            }
        }
        return visitorId;
    }

    public void setVisitorId(UUID id) {
        this.visitorId = id;
        this.entityData.set(DATA_VISITOR_ID, id != null ? id.toString() : "");
    }

    // --- Interaction ---

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        UUID vid = getVisitorId();
        if (vid == null) return InteractionResult.PASS;

        ServerLevel level = (ServerLevel) this.level();
        boolean isAdmin = AdminSystem.isAdmin(serverPlayer);
        String factionId = FactionManager.get(level).getPlayerFaction(serverPlayer.getUUID());
        if (factionId == null && !isAdmin) {
            serverPlayer.sendSystemMessage(Component.literal(
                "\u00A7cYou must be in a faction to recruit visitors."));
            return InteractionResult.SUCCESS;
        }
        // Admin without a faction: use the first available faction
        if (factionId == null && isAdmin) {
            var allFactions = FactionManager.get(level).getAllFactions();
            if (!allFactions.isEmpty()) {
                factionId = allFactions.iterator().next().getFactionId();
            } else {
                serverPlayer.sendSystemMessage(Component.literal(
                    "\u00A7cNo factions exist on the server."));
                return InteractionResult.SUCCESS;
            }
        }

        VisitorManager mgr = VisitorManager.get(level, factionId);
        VisitorData data = mgr.getVisitor(vid);
        if (data == null) {
            serverPlayer.sendSystemMessage(Component.literal(
                "\u00A77This visitor has already left."));
            this.discard();
            return InteractionResult.SUCCESS;
        }

        if (data.isRecruited()) {
            serverPlayer.sendSystemMessage(Component.literal(
                "\u00A77This visitor has already been recruited."));
            return InteractionResult.SUCCESS;
        }

        // Show visitor info and recruitment cost in chat
        // (A proper GUI screen could be added later; for now, chat-based dialog)
        serverPlayer.sendSystemMessage(Component.literal(
            "\u00A76\u00A7l--- Visitor: " + data.getName() + " ---"));
        serverPlayer.sendSystemMessage(Component.literal(
            "\u00A77" + data.getBiography()));
        serverPlayer.sendSystemMessage(Component.literal(
            "\u00A7eTier: \u00A7f" + data.getRecruitTier() + " \u00A77| \u00A7eCost: \u00A7f" + data.getCostDisplayString()));

        // Admin bypass: recruit immediately without cost
        if (isAdmin) {
            mgr.recruitVisitor(vid, serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal(
                "\u00A7a[Admin] Visitor recruited for free!"));
            return InteractionResult.SUCCESS;
        }

        serverPlayer.sendSystemMessage(Component.literal(
            "\u00A7aRight-click again with the required items to recruit!"));

        // Attempt recruitment on second click (if player has items)
        ItemStack cost = data.getRecruitCost();
        if (!cost.isEmpty()) {
            int found = 0;
            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                ItemStack slot = serverPlayer.getInventory().getItem(i);
                if (slot.is(cost.getItem())) {
                    found += slot.getCount();
                }
            }
            if (found >= cost.getCount()) {
                mgr.recruitVisitor(vid, serverPlayer);
            }
        } else {
            // Free visitor — recruit immediately
            mgr.recruitVisitor(vid, serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }

    // --- Peaceful ---

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Visitors are invulnerable to player damage
        if (source.getEntity() instanceof Player) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // --- NBT ---

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (visitorId != null) {
            output.putString("VisitorId", visitorId.toString());
        }
        output.putBoolean("Sitting", isSitting());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String vid = input.getStringOr("VisitorId", "");
        if (!vid.isEmpty()) {
            try {
                this.visitorId = UUID.fromString(vid);
                this.entityData.set(DATA_VISITOR_ID, vid);
            } catch (IllegalArgumentException ignored) {}
        }
        setSitting(input.getBooleanOr("Sitting", true));
    }
}
