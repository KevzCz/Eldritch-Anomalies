package net.pixeldreamstudios.eldritch_anomalies.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "eldritch-anomalies")
public class EldritchAnomaliesConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public GiganticConfig giganticConfig = new GiganticConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public CrusherConfig crusherConfig = new CrusherConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public MeteorConfig meteorConfig = new MeteorConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public AdaptiveConfig adaptiveConfig = new AdaptiveConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public VortexConfig vortexConfig = new VortexConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public SiphonConfig siphonConfig = new SiphonConfig();

    public static class GiganticConfig {
        public String name = "Gigantic";
        public boolean disabled = false;
        public double sizeMultiplier = 1.0;
        public double damageMultiplier = 1.5;
    }

    public static class CrusherConfig {
        public String name = "Crusher";
        public boolean disabled = false;
        public long cooldown = 400;
        public double knockbackStrength = 2.0;
        public int slownessDuration = 100;
        public int slownessAmplifier = 2;
    }

    public static class MeteorConfig {
        public String name = "Meteor";
        public boolean disabled = false;
        public long cooldown = 300;
        public float castRange = 16.0F;
        public double firePowerBonus = 10.0;
    }

    public static class AdaptiveConfig {
        public String name = "Adaptive";
        public boolean disabled = false;
        public int maxStacks = 10;
        public long adaptationDecayTime = 100;
        public float weaknessMultiplier = 1.15F;
        public double armorPerStack = 2.0;
        public double resistancePerStack = 3.5;
        public double knockbackResistancePerStack = 0.1;
    }

    public static class VortexConfig {
        public String name = "Vortex";
        public boolean disabled = false;
        public long cooldown = 250;
        public double pullRadius = 12.0;
        public double pullStrength = 0.6;
        public double verticalLiftBoost = 0.3;
        public float damage = 2.0F;
        public double closeDamageDistance = 3.0;
    }

    public static class SiphonConfig {
        public String name = "Siphon";
        public boolean disabled = false;
        public long cooldown = 300;
        public double drainRadius = 10.0;
        public float drainAmount = 2.0F;
        public float healthThreshold = 0.4F;
        public int maxTargets = 5;
        public float minimumHealthLeft = 1.0F;
    }
}