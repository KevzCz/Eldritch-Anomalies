package net.pixeldreamstudios.eldritch_anomalies.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;

public class MobConfigHelper {

    public static MobGroup getGroup(EntityType<?> entityType) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (key == null) return MobGroup.DEFAULT;
        String id = key.toString();
        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;
        if (config.blacklistedMobs.contains(id)) return MobGroup.BLACKLISTED;
        if (config.alwaysEldritchMobs.contains(id)) return MobGroup.ALWAYS_ELDRITCH;
        if (config.alwaysUltraMobs.contains(id)) return MobGroup.ALWAYS_ULTRA;
        if (config.alwaysEliteMobs.contains(id)) return MobGroup.ALWAYS_ELITE;
        if (config.allowedMobs.contains(id)) return MobGroup.ALLOWED;
        return MobGroup.DEFAULT;
    }

    public static String getEntityId(EntityType<?> entityType) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return key != null ? key.toString() : null;
    }
}
