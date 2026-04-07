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

        if (!config.miasmaConfig.disabled) {
            AbilityHelper.addAbility(new MiasmaAbility());
        }

        if (!config.collapseConfig.disabled) {
            AbilityHelper.addAbility(new CollapseAbility());
        }

        if (!config.fractureConfig.disabled) {
            AbilityHelper.addAbility(new FractureAbility());
        }

        if (!config.eclipseConfig.disabled) {
            AbilityHelper.addAbility(new EclipseAbility());
        }

        if (!config.poltergeistConfig.disabled) {
            AbilityHelper.addAbility(new PoltergeistAbility());
        }

        if (!config.corrosiveConfig.disabled) {
            AbilityHelper.addAbility(new CorrosiveAbility());
        }

        if (!config.phantomStrikeConfig.disabled) {
            AbilityHelper.addAbility(new PhantomStrikeAbility());
        }

        if (!config.anchorConfig.disabled) {
            AbilityHelper.addAbility(new AnchorAbility());
        }

        if (!config.cursedTouchConfig.disabled) {
            AbilityHelper.addAbility(new CursedTouchAbility());
        }

        if (!config.temporalSurgeConfig.disabled) {
            AbilityHelper.addAbility(new TemporalSurgeAbility());
        }

        if (!config.stampedeConfig.disabled) {
            AbilityHelper.addAbility(new StampedeAbility());
        }

        if (!config.bulwarkConfig.disabled) {
            AbilityHelper.addAbility(new BulwarkAbility());
        }

        if (!config.sporeCloudConfig.disabled) {
            AbilityHelper.addAbility(new SporeCloudAbility());
        }

        if (!config.maliceConfig.disabled) {
            AbilityHelper.addAbility(new MaliceAbility());
        }

        if (!config.voidFuryConfig.disabled) {
            AbilityHelper.addAbility(new VoidFuryAbility());
        }

        AbilityHelper.removeDisabledAbilities();
    }
}