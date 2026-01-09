package net.pixeldreamstudios.eldritch_anomalies;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EldritchAnomalies implements ModInitializer {
	public static final String MOD_ID = "eldritch-anomalies";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EldritchAnomaliesConfig ELDRITCH_ANOMALIES_CONFIG =
			AutoConfig.register(EldritchAnomaliesConfig.class, JanksonConfigSerializer:: new).getConfig();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Eldritch Anomalies addon");
		DynamicTagRegistry.register();
		AbilityRegistry.register();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}