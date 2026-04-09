package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry that maps {@link CitizenJob} enum values to {@link IJob} factory methods.
 * When a citizen is assigned a job via the {@link com.ultra.megamod.feature.citizen.entity.mc.handlers.CitizenJobHandler},
 * this registry creates the appropriate {@link IJob} instance with its AI.
 * <p>
 * Usage:
 * <pre>
 * IJob job = JobRegistry.createJob(CitizenJob.MINER, citizenEntity);
 * if (job != null) {
 *     job.onStart();
 * }
 * </pre>
 */
public final class JobRegistry {

    /** Map of CitizenJob enum -> factory function that creates IJob instances. */
    private static final Map<CitizenJob, Function<MCEntityCitizen, IJob>> FACTORIES = new EnumMap<>(CitizenJob.class);

    static {
        // Register all worker job factories
        register(CitizenJob.MINER, JobMiner::new);
        register(CitizenJob.FARMER, JobFarmer::new);
        register(CitizenJob.BUILDER, JobBuilder::new);
        register(CitizenJob.DELIVERYMAN, JobDeliveryman::new);
        register(CitizenJob.FISHERMAN, JobFisherman::new);

        // Future job implementations can be registered here:
        // register(CitizenJob.LUMBERJACK, JobLumberjack::new);
        // register(CitizenJob.BAKER, JobBaker::new);
        // register(CitizenJob.BLACKSMITH, JobBlacksmith::new);
        // etc.
    }

    private JobRegistry() {
        // Utility class
    }

    /**
     * Register a job factory for a given CitizenJob type.
     *
     * @param jobType the job enum value
     * @param factory function that creates an IJob given a citizen entity
     */
    public static void register(@NotNull CitizenJob jobType, @NotNull Function<MCEntityCitizen, IJob> factory) {
        FACTORIES.put(jobType, factory);
    }

    /**
     * Create a new IJob instance for the given job type and citizen.
     *
     * @param jobType the job to create
     * @param citizen the citizen entity to assign it to
     * @return the IJob instance, or null if no factory is registered for this job type
     */
    @Nullable
    public static IJob createJob(@NotNull CitizenJob jobType, @NotNull MCEntityCitizen citizen) {
        Function<MCEntityCitizen, IJob> factory = FACTORIES.get(jobType);
        if (factory == null) {
            return null;
        }
        return factory.apply(citizen);
    }

    /**
     * Check if a job type has a registered AI implementation.
     *
     * @param jobType the job to check
     * @return true if an AI factory is registered
     */
    public static boolean hasAI(@NotNull CitizenJob jobType) {
        return FACTORIES.containsKey(jobType);
    }

    /**
     * Get the number of registered job types with AI.
     *
     * @return count of registered job factories
     */
    public static int getRegisteredCount() {
        return FACTORIES.size();
    }
}
