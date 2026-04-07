package net.pixeldreamstudios.eldritch_anomalies.config;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;

import java.util.Set;

public class DatapackSyncer {

    public static void syncIfNeeded(
            Set<ResourceLocation> allowedByTag,
            Set<ResourceLocation> blacklistByTag,
            Set<ResourceLocation> alwaysEliteByTag,
            Set<ResourceLocation> alwaysUltraByTag,
            Set<ResourceLocation> alwaysEldritchByTag,
            Set<ResourceLocation> dynamicAllowed
    ) {
        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;
        if (config.datapackSyncDone) return;

        for (ResourceLocation id : blacklistByTag) {
            String s = id.toString();
            if (!hasAnyEntry(config, s)) config.blacklistedMobs.add(s);
        }
        for (ResourceLocation id : alwaysEldritchByTag) {
            String s = id.toString();
            if (!hasAnyEntry(config, s)) config.alwaysEldritchMobs.add(s);
        }
        for (ResourceLocation id : alwaysUltraByTag) {
            String s = id.toString();
            if (!hasAnyEntry(config, s)) config.alwaysUltraMobs.add(s);
        }
        for (ResourceLocation id : alwaysEliteByTag) {
            String s = id.toString();
            if (!hasAnyEntry(config, s)) config.alwaysEliteMobs.add(s);
        }
        for (ResourceLocation id : allowedByTag) {
            String s = id.toString();
            if (!hasAnyEntry(config, s)) config.allowedMobs.add(s);
        }
        for (ResourceLocation id : dynamicAllowed) {
            String s = id.toString();
            if (!hasAnyEntry(config, s)) config.allowedMobs.add(s);
        }

        config.datapackSyncDone = true;
        AutoConfig.getConfigHolder(EldritchAnomaliesConfig.class).save();
        EldritchAnomalies.LOGGER.info("Eldritch Anomalies: synced datapack tags into config");
    }

    public static void forceSyncFromDatapacks(
            Set<ResourceLocation> allowedByTag,
            Set<ResourceLocation> blacklistByTag,
            Set<ResourceLocation> alwaysEliteByTag,
            Set<ResourceLocation> alwaysUltraByTag,
            Set<ResourceLocation> alwaysEldritchByTag,
            Set<ResourceLocation> dynamicAllowed
    ) {
        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;

        config.blacklistedMobs.clear();
        config.alwaysEldritchMobs.clear();
        config.alwaysUltraMobs.clear();
        config.alwaysEliteMobs.clear();
        config.allowedMobs.clear();

        blacklistByTag.forEach(id -> config.blacklistedMobs.add(id.toString()));
        alwaysEldritchByTag.forEach(id -> config.alwaysEldritchMobs.add(id.toString()));
        alwaysUltraByTag.forEach(id -> config.alwaysUltraMobs.add(id.toString()));
        alwaysEliteByTag.forEach(id -> config.alwaysEliteMobs.add(id.toString()));

        for (ResourceLocation id : allowedByTag) {
            String s = id.toString();
            if (!config.blacklistedMobs.contains(s)) config.allowedMobs.add(s);
        }
        for (ResourceLocation id : dynamicAllowed) {
            String s = id.toString();
            if (!config.blacklistedMobs.contains(s) && !config.allowedMobs.contains(s)) config.allowedMobs.add(s);
        }

        AutoConfig.getConfigHolder(EldritchAnomaliesConfig.class).save();
        EldritchAnomalies.LOGGER.info("Eldritch Anomalies: force-synced datapack tags into config");
    }

    private static boolean hasAnyEntry(EldritchAnomaliesConfig config, String id) {
        return config.blacklistedMobs.contains(id)
                || config.alwaysEldritchMobs.contains(id)
                || config.alwaysUltraMobs.contains(id)
                || config.alwaysEliteMobs.contains(id)
                || config.allowedMobs.contains(id);
    }
}
