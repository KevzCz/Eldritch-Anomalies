package net.pixeldreamstudios.eldritch_anomalies;

import net.hyper_pigeon.eldritch_mobs.ability.AbilityHelper;
import net.pixeldreamstudios.eldritch_anomalies.ability.*;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

public class AbilityRegistry {

    public static void register() {
        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;

        if (! config.giganticConfig.disabled) {
            AbilityHelper.addAbility(new GiganticAbility());
        }

        if (!config.crusherConfig.disabled) {
            AbilityHelper.addAbility(new CrusherAbility());
        }

        if (!config.adaptiveConfig.disabled) {
            AbilityHelper.addAbility(new AdaptiveAbility());
        }

        if (!config.vortexConfig.disabled) {
            AbilityHelper.addAbility(new VortexAbility());
        }

        if (!config.siphonConfig.disabled) {
            AbilityHelper.addAbility(new SiphonAbility());
        }

        if (!config.meteorConfig.disabled && MeteorAbility.isAvailable()) {
            AbilityHelper.addAbility(new MeteorAbility());
        }

        AbilityHelper.removeDisabledAbilities();
    }
}