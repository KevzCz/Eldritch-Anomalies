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

import java.io.InputStreamReader;
import java.util.*;

public class DynamicTagRegistry implements SimpleSynchronousResourceReloadListener {

    private static final ResourceLocation LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath("eldritch_anomalies", "dynamic_tags");

    private static final Set<ResourceLocation> ALLOWED_ENTITIES = new HashSet<>();
    private static boolean initialized = false;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(DynamicTagRegistry::onServerStarting);

        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new DynamicTagRegistry());
    }

    private static void onServerStarting(MinecraftServer server) {
        if (! initialized) {
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

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                "conditional_tags/entity_type",
                id -> id.getPath().endsWith(".json") &&
                        id.getNamespace().equals("eldritch_anomalies")
        );


        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            String filename = location.getPath()
                    .replace("conditional_tags/entity_type/", "")
                    .replace(".json", "");

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String modId = json.has("mod_id") ? json.get("mod_id").getAsString() : filename;

                if (FabricLoader.getInstance().isModLoaded(modId)) {
                    loadEntitiesFromJson(json, modId);
                } else {
                    EldritchAnomalies.LOGGER.info("Skipping '{}' - mod not loaded", modId);
                }
            } catch (Exception e) {
                EldritchAnomalies.LOGGER.error("Failed to parse:  {}", location, e);
            }
        }
    }

    private void loadEntitiesFromJson(JsonObject json, String modId) {
        if (json.has("values") && json.get("values").isJsonArray()) {
            JsonArray values = json.getAsJsonArray("values");
            int count = 0;

            for (JsonElement element : values) {
                if (element.isJsonPrimitive()) {
                    String entityId = element.getAsString();

                    if (entityId.startsWith("#")) {
                        EldritchAnomalies.LOGGER.warn("Tag references not yet supported: {}", entityId);
                        continue;
                    }

                    try {
                        ResourceLocation entityLocation = ResourceLocation.parse(entityId);

                        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityLocation)) {
                            ALLOWED_ENTITIES.add(entityLocation);
                            count++;
                            EldritchAnomalies.LOGGER.debug("Added entity: {}", entityLocation);
                        } else {
                            EldritchAnomalies.LOGGER.warn("Entity type not found: {}", entityId);
                        }
                    } catch (Exception e) {
                        EldritchAnomalies.LOGGER.warn("Invalid entity ID: {} - {}", entityId, e.getMessage());
                    }
                }
            }

        }
    }

    public static boolean isEntityAllowed(EntityType<?> entityType) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        boolean allowed = ALLOWED_ENTITIES.contains(id);

        return allowed;
    }

    public static Set<ResourceLocation> getAllowedEntities() {
        return Collections.unmodifiableSet(ALLOWED_ENTITIES);
    }
}