package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.fabricmc.loader.api.FabricLoader;
import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;
import net.spell_power.api.SpellResistance;

public class AdaptiveAbility implements Ability {

    private final EldritchAnomaliesConfig.AdaptiveConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.adaptiveConfig;
    private static final boolean spellPowerAvailable = FabricLoader.getInstance().isModLoaded("spell_power");

    private DamageAdaptation currentAdaptation = DamageAdaptation.NONE;
    private int adaptationStacks = 0;
    private long lastDamageTime = 0;

    @Override
    public String getName() {
        return config.name;
    }

    @Override
    public AbilityType getAbilityType() {
        return AbilityType.PASSIVE;
    }

    @Override
    public AbilitySubType getAbilitySubType() {
        return AbilitySubType.DEFENSIVE;
    }

    @Override
    public void onDamaged(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        long currentTime = mob.level().getGameTime();
        DamageAdaptation incomingType = getDamageType(source);

        if (currentTime - lastDamageTime > config.adaptationDecayTime) {
            resetAdaptation(mob);
        }

        if (incomingType != currentAdaptation) {
            if (currentAdaptation != DamageAdaptation.NONE) {
                float extraDamage = amount * (config.weaknessMultiplier - 1.0F);
                mob.hurt(source, extraDamage);
            }

            resetAdaptation(mob);
            currentAdaptation = incomingType;
        }

        if (adaptationStacks < config.maxStacks && incomingType != DamageAdaptation.NONE) {
            adaptationStacks++;
            applyAdaptation(mob, incomingType);
        }

        lastDamageTime = currentTime;
    }

    @Override
    public void passiveApply(Mob mobEntity) {
        long currentTime = mobEntity.level().getGameTime();

        if (currentAdaptation != DamageAdaptation.NONE &&
                currentTime - lastDamageTime > config.adaptationDecayTime) {
            resetAdaptation(mobEntity);
        }
    }

    private DamageAdaptation getDamageType(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return DamageAdaptation.FIRE;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return DamageAdaptation.EXPLOSION;
        }
        if (spellPowerAvailable && source.is(SpellResistance.Attributes.GENERIC.damageTypes)) {
            return DamageAdaptation.MAGIC;
        }
        if (! source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return DamageAdaptation.PHYSICAL;
        }
        return DamageAdaptation.NONE;
    }

    private void applyAdaptation(Mob mob, DamageAdaptation type) {
        switch (type) {
            case PHYSICAL -> applyArmorModifier(mob);
            case MAGIC -> applyResistanceModifier(mob);
            case FIRE -> {
                int duration = adaptationStacks * 20;
                mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
            }
            case EXPLOSION -> applyKnockbackResistance(mob);
        }
    }

    private void resetAdaptation(Mob mob) {
        removeArmorModifier(mob);
        removeResistanceModifier(mob);
        removeKnockbackModifier(mob);

        currentAdaptation = DamageAdaptation.NONE;
        adaptationStacks = 0;
    }

    private void applyArmorModifier(Mob mob) {
        AttributeInstance armorAttr = mob.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(EldritchAnomalies.id("adaptive_armor"));
            AttributeModifier modifier = new AttributeModifier(
                    EldritchAnomalies.id("adaptive_armor"),
                    adaptationStacks * config.armorPerStack,
                    AttributeModifier.Operation.ADD_VALUE
            );
            armorAttr.addPermanentModifier(modifier);
        }
    }

    private void removeArmorModifier(Mob mob) {
        AttributeInstance armorAttr = mob.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(EldritchAnomalies.id("adaptive_armor"));
        }
    }

    private void applyResistanceModifier(Mob mob) {
        if (! spellPowerAvailable) {
            return;
        }

        try {
            SpellResistance.Attributes.Entry genericResistance = SpellResistance.Attributes.GENERIC;
            if (genericResistance.attributeEntry != null) {
                AttributeInstance resistanceAttr = mob.getAttribute(genericResistance.attributeEntry);
                if (resistanceAttr != null) {
                    resistanceAttr.removeModifier(EldritchAnomalies.id("adaptive_resistance"));
                    AttributeModifier modifier = new AttributeModifier(
                            EldritchAnomalies.id("adaptive_resistance"),
                            adaptationStacks * config.resistancePerStack,
                            AttributeModifier.Operation.ADD_VALUE
                    );
                    resistanceAttr.addPermanentModifier(modifier);
                }
            }
        } catch (Exception e) {
            EldritchAnomalies.LOGGER.error("Failed to apply resistance modifier", e);
        }
    }

    private void removeResistanceModifier(Mob mob) {
        if (!spellPowerAvailable) {
            return;
        }

        try {
            SpellResistance.Attributes.Entry genericResistance = SpellResistance.Attributes.GENERIC;
            if (genericResistance.attributeEntry != null) {
                AttributeInstance resistanceAttr = mob.getAttribute(genericResistance.attributeEntry);
                if (resistanceAttr != null) {
                    resistanceAttr.removeModifier(EldritchAnomalies.id("adaptive_resistance"));
                }
            }
        } catch (Exception e) {
            EldritchAnomalies.LOGGER.error("Failed to remove resistance modifier", e);
        }
    }

    private void applyKnockbackResistance(Mob mob) {
        AttributeInstance knockbackAttr = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) {
            knockbackAttr.removeModifier(EldritchAnomalies.id("adaptive_knockback"));
            AttributeModifier modifier = new AttributeModifier(
                    EldritchAnomalies.id("adaptive_knockback"),
                    adaptationStacks * config.knockbackResistancePerStack,
                    AttributeModifier.Operation.ADD_VALUE
            );
            knockbackAttr.addPermanentModifier(modifier);
        }
    }

    private void removeKnockbackModifier(Mob mob) {
        AttributeInstance knockbackAttr = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) {
            knockbackAttr.removeModifier(EldritchAnomalies.id("adaptive_knockback"));
        }
    }

    private enum DamageAdaptation {
        NONE,
        PHYSICAL,
        MAGIC,
        FIRE,
        EXPLOSION
    }
}