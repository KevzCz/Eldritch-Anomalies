package net.pixeldreamstudios.eldritch_anomalies.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;

@Config(name = "eldritch-anomalies")
public class EldritchAnomaliesConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public boolean datapackSyncDone = false;

    @ConfigEntry.Gui.Excluded
    public ArrayList<String> allowedMobs = new ArrayList<>();

    @ConfigEntry.Gui.Excluded
    public ArrayList<String> blacklistedMobs = new ArrayList<>();

    @ConfigEntry.Gui.Excluded
    public ArrayList<String> alwaysEliteMobs = new ArrayList<>();

    @ConfigEntry.Gui.Excluded
    public ArrayList<String> alwaysUltraMobs = new ArrayList<>();

    @ConfigEntry.Gui.Excluded
    public ArrayList<String> alwaysEldritchMobs = new ArrayList<>();

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

    @ConfigEntry.Gui.CollapsibleObject
    public MiasmaConfig miasmaConfig = new MiasmaConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public CollapseConfig collapseConfig = new CollapseConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public FractureConfig fractureConfig = new FractureConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EclipseConfig eclipseConfig = new EclipseConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public PoltergeistConfig poltergeistConfig = new PoltergeistConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public CorrosiveConfig corrosiveConfig = new CorrosiveConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public PhantomStrikeConfig phantomStrikeConfig = new PhantomStrikeConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public AnchorConfig anchorConfig = new AnchorConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public CursedTouchConfig cursedTouchConfig = new CursedTouchConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public TemporalSurgeConfig temporalSurgeConfig = new TemporalSurgeConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public StampedeConfig stampedeConfig = new StampedeConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public BulwarkConfig bulwarkConfig = new BulwarkConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public SporeCloudConfig sporeCloudConfig = new SporeCloudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public MaliceConfig maliceConfig = new MaliceConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public VoidFuryConfig voidFuryConfig = new VoidFuryConfig();

    public static class GiganticConfig {
        public String name = "Gigantic";
        public boolean disabled = false;
        public double sizeMultiplier = 1.0;
        public double damageMultiplier = 1.5;
        public long tremorInterval = 15L;
        public double tremorRadius = 5.0;
        public double tremorForce = 0.4;
    }

    public static class CrusherConfig {
        public String name = "Crusher";
        public boolean disabled = false;
        public long cooldown = 400;
        public double knockbackStrength = 2.0;
        public int slownessDuration = 100;
        public double slowAmount = 0.4;
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

    public static class MiasmaConfig {
        public String name = "Miasma";
        public boolean disabled = false;
        public long pulseInterval = 40;
        public double radius = 8.0;
        public float damage = 1.5F;
        public int nauseaDuration = 80;
        public double miasmaSlowAmount = 0.35;
    }

    public static class CollapseConfig {
        public String name = "Collapse";
        public boolean disabled = false;
        public long cooldown = 300;
        public double triggerRange = 6.0;
        public double radius = 5.0;
        public double launchForce = 1.2;
        public float damage = 4.0F;
    }

    public static class FractureConfig {
        public String name = "Fracture";
        public boolean disabled = false;
        public long cooldown = 200;
        public double healthReduction = 4.0;
    }

    public static class EclipseConfig {
        public String name = "Eclipse";
        public boolean disabled = false;
        public long cooldown = 400;
        public int strengthDuration = 100;
        public double darkRadius = 12.0;
        public int darknessDuration = 100;
        public double attackBoost = 0.5;
        public double attackDebuff = 0.3;
        public double minDebuffStrength = 0.1;
        public double armorDebuffReduction = 0.5;
    }

    public static class PoltergeistConfig {
        public String name = "Poltergeist";
        public boolean disabled = false;
        public long cooldown = 200;
        public double maxRange = 10.0;
        public double horizontalForce = 1.6;
        public double verticalForce = 0.9;
        public int liftTicks = 25;
        public int holdTicks = 15;
    }

    public static class CorrosiveConfig {
        public String name = "Corrosive";
        public boolean disabled = false;
        public int maxStacks = 5;
        public double armorReductionPerStack = 2.0;
    }

    public static class PhantomStrikeConfig {
        public String name = "Phantom Strike";
        public boolean disabled = false;
        public long flickerInterval = 80;
        public int invisibilityDuration = 20;
        public double strikeTeleportDistance = 1.5;
        public long strikeCooldown = 60L;
    }

    public static class AnchorConfig {
        public String name = "Anchor";
        public boolean disabled = false;
        public double knockbackResistance = 1.0;
        public float lowHealthThreshold = 0.3F;
        public int resistanceAmplifier = 1;
        public double retaliationForce = 1.5;
        public double retaliationSlowAmount = 0.3;
        public long retaliationSlowDuration = 60L;
    }

    public static class CursedTouchConfig {
        public String name = "Cursed Touch";
        public boolean disabled = false;
        public int glowDuration = 200;
        public int maxStacks = 5;
        public float bonusDamagePerStack = 1.5F;
        public long stackDuration = 200L;
    }

    public static class TemporalSurgeConfig {
        public String name = "Temporal Surge";
        public boolean disabled = false;
        public long cooldown = 300;
        public double radius = 10.0;
        public int duration = 80;
        public double speedBoost = 1.5;
        public double slowAmount = 0.6;
    }

    public static class StampedeConfig {
        public String name = "Stampede";
        public boolean disabled = false;
        public long cooldown = 180;
        public double minRange = 4.0;
        public double maxRange = 20.0;
        public double chargeSpeed = 1.8;
        public double impactRange = 3.0;
        public float impactDamage = 6.0F;
        public double impactKnockback = 1.2;
        public long chargeDuration = 20L;
    }

    public static class BulwarkConfig {
        public String name = "Bulwark";
        public boolean disabled = false;
        public double healthMultiplier = 1.0;
        public double armorBonus = 8.0;
    }

    public static class SporeCloudConfig {
        public String name = "Spore Cloud";
        public boolean disabled = false;
        public long cooldown = 250;
        public double radius = 8.0;
        public long sporeDuration = 100L;
        public double sporeSlowAmount = 0.4;
        public float tickDamage = 1.5F;
        public long tickDamageInterval = 20L;
    }

    public static class MaliceConfig {
        public String name = "Malice";
        public boolean disabled = false;
        public double missingHealthPercent = 0.1;
        public float minimumBonusDamage = 1.0F;
    }

    public static class VoidFuryConfig {
        public String name = "Void Fury";
        public boolean disabled = false;
        public double radius = 8.0;
        public float damage = 12.0F;
    }
}