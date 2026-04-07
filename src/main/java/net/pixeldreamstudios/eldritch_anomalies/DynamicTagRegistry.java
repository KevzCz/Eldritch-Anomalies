package net.pixeldreamstudios.eldritch_anomalies;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.pixeldreamstudios.eldritch_anomalies.config.DatapackSyncer;

import java.io.InputStreamReader;
import java.util.*;

public class DynamicTagRegistry implements SimpleSynchronousResourceReloadListener {

    private static final ResourceLocation LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath("eldritch_anomalies", "dynamic_tags");

    private static final Set<ResourceLocation> ALLOWED_ENTITIES = new HashSet<>();
    private static final Set<ResourceLocation> BLACKLISTED_ENTITIES = new HashSet<>();
    private static final Set<ResourceLocation> ALWAYS_ELITE_ENTITIES = new HashSet<>();
    private static final Set<ResourceLocation> ALWAYS_ULTRA_ENTITIES = new HashSet<>();
    private static final Set<ResourceLocation> ALWAYS_ELDRITCH_ENTITIES = new HashSet<>();
    private static boolean initialized = false;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(DynamicTagRegistry::onServerStarting);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new DynamicTagRegistry());
    }

    private static void onServerStarting(MinecraftServer server) {
        if (!initialized) {
            new DynamicTagRegistry().onResourceManagerReload(server.getResourceManager());
            initialized = true;
        }
    }

    @Override
    public ResourceLocation getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ALLOWED_ENTITIES.clear();
        BLACKLISTED_ENTITIES.clear();
        ALWAYS_ELITE_ENTITIES.clear();
        ALWAYS_ULTRA_ENTITIES.clear();
        ALWAYS_ELDRITCH_ENTITIES.clear();

        Map<ResourceLocation, Resource> dynamicResources = resourceManager.listResources(
                "conditional_tags/entity_type",
                id -> id.getPath().endsWith(".json") && id.getNamespace().equals("eldritch_anomalies")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : dynamicResources.entrySet()) {
            ResourceLocation location = entry.getKey();
            String filename = location.getPath()
                    .replace("conditional_tags/entity_type/", "")
                    .replace(".json", "");
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String modId = json.has("mod_id") ? json.get("mod_id").getAsString() : filename;
                if (FabricLoader.getInstance().isModLoaded(modId)) {
                    loadEntities(json, ALLOWED_ENTITIES);
                } else {
                    EldritchAnomalies.LOGGER.info("Skipping '{}' - mod not loaded", modId);
                }
            } catch (Exception e) {
                EldritchAnomalies.LOGGER.error("Failed to parse: {}", location, e);
            }
        }

        readEldritchMobsTag(resourceManager, "tags/entity_type/allowed.json", ALLOWED_ENTITIES);
        readEldritchMobsTag(resourceManager, "tags/entity_type/blacklist.json", BLACKLISTED_ENTITIES);
        readEldritchMobsTag(resourceManager, "tags/entity_type/always_elite.json", ALWAYS_ELITE_ENTITIES);
        readEldritchMobsTag(resourceManager, "tags/entity_type/always_ultra.json", ALWAYS_ULTRA_ENTITIES);
        readEldritchMobsTag(resourceManager, "tags/entity_type/always_eldritch.json", ALWAYS_ELDRITCH_ENTITIES);

        DatapackSyncer.syncIfNeeded(
                Collections.unmodifiableSet(ALLOWED_ENTITIES),
                Collections.unmodifiableSet(BLACKLISTED_ENTITIES),
                Collections.unmodifiableSet(ALWAYS_ELITE_ENTITIES),
                Collections.unmodifiableSet(ALWAYS_ULTRA_ENTITIES),
                Collections.unmodifiableSet(ALWAYS_ELDRITCH_ENTITIES),
                Collections.unmodifiableSet(ALLOWED_ENTITIES)
        );
    }

    private void readEldritchMobsTag(ResourceManager resourceManager, String path, Set<ResourceLocation> target) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("eldritch_mobs", path);
        Optional<Resource> resource = resourceManager.getResource(loc);
        if (resource.isEmpty()) return;
        try (InputStreamReader reader = new InputStreamReader(resource.get().open())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            loadEntities(json, target);
        } catch (Exception e) {
            EldritchAnomalies.LOGGER.warn("Could not read eldritch_mobs tag {}: {}", path, e.getMessage());
        }
    }

    private void loadEntities(JsonObject json, Set<ResourceLocation> target) {
        if (!json.has("values") || !json.get("values").isJsonArray()) return;
        JsonArray values = json.getAsJsonArray("values");
        for (JsonElement element : values) {
            if (!element.isJsonPrimitive()) continue;
            String entityId = element.getAsString();
            if (entityId.startsWith("#")) continue;
            try {
                ResourceLocation entityLocation = ResourceLocation.parse(entityId);
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityLocation)) {
                    target.add(entityLocation);
                }
            } catch (Exception ignored) {}
        }
    }

    public static boolean isEntityAllowed(EntityType<?> entityType) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return ALLOWED_ENTITIES.contains(id);
    }

    public static Set<ResourceLocation> getAllowedEntities() {
        return Collections.unmodifiableSet(ALLOWED_ENTITIES);
    }

    public static Set<ResourceLocation> getBlacklistedEntities() {
        return Collections.unmodifiableSet(BLACKLISTED_ENTITIES);
    }

    public static Set<ResourceLocation> getAlwaysEliteEntities() {
        return Collections.unmodifiableSet(ALWAYS_ELITE_ENTITIES);
    }

    public static Set<ResourceLocation> getAlwaysUltraEntities() {
        return Collections.unmodifiableSet(ALWAYS_ULTRA_ENTITIES);
    }

    public static Set<ResourceLocation> getAlwaysEldritchEntities() {
        return Collections.unmodifiableSet(ALWAYS_ELDRITCH_ENTITIES);
    }

    public static void forceSyncFromDatapacks() {
        DatapackSyncer.forceSyncFromDatapacks(
                Collections.unmodifiableSet(ALLOWED_ENTITIES),
                Collections.unmodifiableSet(BLACKLISTED_ENTITIES),
                Collections.unmodifiableSet(ALWAYS_ELITE_ENTITIES),
                Collections.unmodifiableSet(ALWAYS_ULTRA_ENTITIES),
                Collections.unmodifiableSet(ALWAYS_ELDRITCH_ENTITIES),
                Collections.unmodifiableSet(ALLOWED_ENTITIES)
        );
    }
}
