package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import com.ultra.megamod.feature.citizen.job.ai.EntityAIWorkDeliveryman;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Deliveryman/courier job. Assigns the deliveryman AI which picks up items
 * from warehouses and delivers them to requesting buildings.
 * <p>
 * Ported from MineColonies' JobDeliveryman, greatly simplified since MegaMod
 * doesn't have the full request system yet. Uses a simple delivery queue.
 */
public class JobDeliveryman extends AbstractJob {

    private static final String TAG_DELIVERY_QUEUE = "DeliveryQueue";

    /**
     * Simple delivery task record.
     */
    public static class DeliveryTask {
        private final BlockPos source;
        private final BlockPos destination;
        private final String description;

        public DeliveryTask(BlockPos source, BlockPos destination, String description) {
            this.source = source;
            this.destination = destination;
            this.description = description;
        }

        public BlockPos getSource() { return source; }
        public BlockPos getDestination() { return destination; }
        public String getDescription() { return description; }
    }

    /** Queue of pending delivery tasks. */
    private final LinkedList<DeliveryTask> deliveryQueue = new LinkedList<>();

    /** The task currently being executed. */
    @Nullable
    private DeliveryTask currentTask = null;

    public JobDeliveryman(@NotNull MCEntityCitizen citizen) {
        super(citizen);
    }

    @Override
    @NotNull
    public CitizenJob getJobType() {
        return CitizenJob.DELIVERYMAN;
    }

    @Override
    @NotNull
    public AbstractEntityAIBasic createAI() {
        return new EntityAIWorkDeliveryman(this);
    }

    // ---- Delivery queue management ----

    public void addDelivery(@NotNull DeliveryTask task) {
        deliveryQueue.add(task);
    }

    @Nullable
    public DeliveryTask getCurrentTask() { return currentTask; }

    @Nullable
    public DeliveryTask pollNextTask() {
        currentTask = deliveryQueue.poll();
        return currentTask;
    }

    public void completeCurrentTask() {
        currentTask = null;
    }

    public boolean hasDeliveries() {
        return !deliveryQueue.isEmpty() || currentTask != null;
    }

    public int getPendingDeliveryCount() {
        return deliveryQueue.size() + (currentTask != null ? 1 : 0);
    }

    // ---- Persistence ----

    @Override
    protected void saveJobData(@NotNull CompoundTag tag) {
        ListTag queueList = new ListTag();
        for (DeliveryTask task : deliveryQueue) {
            CompoundTag taskTag = new CompoundTag();
            CompoundTag srcTag = new CompoundTag();
            srcTag.putInt("X", task.source.getX());
            srcTag.putInt("Y", task.source.getY());
            srcTag.putInt("Z", task.source.getZ());
            taskTag.put("Source", srcTag);

            CompoundTag destTag = new CompoundTag();
            destTag.putInt("X", task.destination.getX());
            destTag.putInt("Y", task.destination.getY());
            destTag.putInt("Z", task.destination.getZ());
            taskTag.put("Dest", destTag);

            taskTag.putString("Desc", task.description);
            queueList.add(taskTag);
        }
        tag.put(TAG_DELIVERY_QUEUE, queueList);
    }

    @Override
    protected void loadJobData(@NotNull CompoundTag tag) {
        deliveryQueue.clear();
        if (tag.contains(TAG_DELIVERY_QUEUE)) {
            ListTag queueList = tag.getListOrEmpty(TAG_DELIVERY_QUEUE);
            for (int i = 0; i < queueList.size(); i++) {
                Tag entry = queueList.get(i);
                if (entry instanceof CompoundTag taskTag) {
                    CompoundTag srcTag = taskTag.getCompoundOrEmpty("Source");
                    CompoundTag destTag = taskTag.getCompoundOrEmpty("Dest");
                    BlockPos src = new BlockPos(
                            srcTag.getIntOr("X", 0),
                            srcTag.getIntOr("Y", 0),
                            srcTag.getIntOr("Z", 0));
                    BlockPos dest = new BlockPos(
                            destTag.getIntOr("X", 0),
                            destTag.getIntOr("Y", 0),
                            destTag.getIntOr("Z", 0));
                    String desc = taskTag.getStringOr("Desc", "delivery");
                    deliveryQueue.add(new DeliveryTask(src, dest, desc));
                }
            }
        }
    }
}
