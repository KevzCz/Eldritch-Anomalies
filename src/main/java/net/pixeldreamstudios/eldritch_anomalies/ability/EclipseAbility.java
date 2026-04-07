package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.fabricmc.loader.api.FabricLoader;
import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.List;
import java.util.WeakHashMap;

public class EclipseAbility implements Ability {

    private final EldritchAnomaliesConfig.EclipseConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.eclipseConfig;

    private long nextUseTime = 0;
    private long boostExpiry = 0;
    private final WeakHashMap<LivingEntity, Long> debuffedTargets = new WeakHashMap<>();

    private static final boolean rangedWeaponAvailable = FabricLoader.getInstance().isModLoaded("ranged_weapon");
    private static final boolean spellPowerAvailable = FabricLoader.getInstance().isModLoaded("spell_power");

    private static final ResourceLocation ECLIPSE_BOOST_ID =
            EldritchAnomalies.id("eclipse_attack_boost");
    private static final ResourceLocation ECLIPSE_DEBUFF_ID =
            EldritchAnomalies.id("eclipse_attack_debuff");
    private static final ResourceLocation ECLIPSE_RANGED_BOOST_ID =
            EldritchAnomalies.id("eclipse_ranged_boost");
    private static final ResourceLocation ECLIPSE_SPELL_BOOST_ID =
            EldritchAnomalies.id("eclipse_spell_boost");
    private static final ResourceLocation ECLIPSE_RANGED_DEBUFF_ID =
            EldritchAnomalies.id("eclipse_ranged_debuff");
    private static final ResourceLocation ECLIPSE_SPELL_DEBUFF_ID =
            EldritchAnomalies.id("eclipse_spell_debuff");

    private double computeEffectiveDebuff(LivingEntity target) {
        float armorFactor = Math.min(target.getArmorValue() / 30.0f, 1.0f);
        double debuff = config.attackDebuff * (1.0 - armorFactor * config.armorDebuffReduction);
        return Math.max(debuff, config.minDebuffStrength);
    }

    @Override
    public String getName() { return config.name; }

    @Override
    public AbilityType getAbilityType() { return AbilityType.ACTIVE; }

    @Override
    public AbilitySubType getAbilitySubType() { return AbilitySubType.OFFENSIVE; }

    @Override
    public long getCooldown() { return config.cooldown; }

    @Override
    public boolean canUseAbility(Mob mob) {
        return !mob.level().isClientSide
                && mob.level().getGameTime() > nextUseTime
                && mob.getTarget() != null;
    }

    @Override
    public void onAbilityUse(Mob mob) {
        if (mob.level().isClientSide || !canUseAbility(mob)) return;
        long now = mob.level().getGameTime();

        AttributeInstance attackAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null && !attackAttr.hasModifier(ECLIPSE_BOOST_ID)) {
            attackAttr.addTransientModifier(new AttributeModifier(
                    ECLIPSE_BOOST_ID, config.attackBoost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        if (rangedWeaponAvailable) {
            var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
            rangedAttr.ifPresent(attr -> {
                AttributeInstance inst = mob.getAttribute(attr);
                if (inst != null && !inst.hasModifier(ECLIPSE_RANGED_BOOST_ID)) {
                    inst.addTransientModifier(new AttributeModifier(
                            ECLIPSE_RANGED_BOOST_ID, config.attackBoost,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            });
        }
        if (spellPowerAvailable) {
            var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
            spellAttr.ifPresent(attr -> {
                AttributeInstance inst = mob.getAttribute(attr);
                if (inst != null && !inst.hasModifier(ECLIPSE_SPELL_BOOST_ID)) {
                    inst.addTransientModifier(new AttributeModifier(
                            ECLIPSE_SPELL_BOOST_ID, config.attackBoost,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            });
        }
        boostExpiry = now + config.strengthDuration;

        AABB area = mob.getBoundingBox().inflate(config.darkRadius);
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(
                LivingEntity.class, area, e -> e != mob && !e.isAlliedTo(mob));

        for (LivingEntity target : nearby) {
            if (mob.distanceTo(target) > config.darkRadius) continue;
            target.addEffect(new MobEffectInstance(
                    MobEffects.DARKNESS, config.darknessDuration, 0, false, true));
            double effectiveDebuff = computeEffectiveDebuff(target);
            AttributeInstance targetAttack = target.getAttribute(Attributes.ATTACK_DAMAGE);
            if (targetAttack != null) {
                targetAttack.removeModifier(ECLIPSE_DEBUFF_ID);
                targetAttack.addTransientModifier(new AttributeModifier(
                        ECLIPSE_DEBUFF_ID, -effectiveDebuff,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            if (rangedWeaponAvailable) {
                var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
                rangedAttr.ifPresent(attr -> {
                    AttributeInstance inst = target.getAttribute(attr);
                    if (inst != null) {
                        inst.removeModifier(ECLIPSE_RANGED_DEBUFF_ID);
                        inst.addTransientModifier(new AttributeModifier(
                                ECLIPSE_RANGED_DEBUFF_ID, -effectiveDebuff,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    }
                });
            }
            if (spellPowerAvailable) {
                var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
                spellAttr.ifPresent(attr -> {
                    AttributeInstance inst = target.getAttribute(attr);
                    if (inst != null) {
                        inst.removeModifier(ECLIPSE_SPELL_DEBUFF_ID);
                        inst.addTransientModifier(new AttributeModifier(
                                ECLIPSE_SPELL_DEBUFF_ID, -effectiveDebuff,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                });
            }
            if (targetAttack != null || rangedWeaponAvailable || spellPowerAvailable) {
                debuffedTargets.put(target, now + config.darknessDuration);
            }
        }

        nextUseTime = now + config.cooldown;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        long now = mob.level().getGameTime();

        if (boostExpiry > 0 && now > boostExpiry) {
            AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) atk.removeModifier(ECLIPSE_BOOST_ID);
            if (rangedWeaponAvailable) {
                var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
                rangedAttr.ifPresent(attr -> {
                    AttributeInstance inst = mob.getAttribute(attr);
                    if (inst != null) inst.removeModifier(ECLIPSE_RANGED_BOOST_ID);
                });
            }
            if (spellPowerAvailable) {
                var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
                spellAttr.ifPresent(attr -> {
                    AttributeInstance inst = mob.getAttribute(attr);
                    if (inst != null) inst.removeModifier(ECLIPSE_SPELL_BOOST_ID);
                });
            }
            boostExpiry = 0;
        }

        debuffedTargets.entrySet().removeIf(entry -> {
            if (now > entry.getValue() || !entry.getKey().isAlive()) {
                LivingEntity t = entry.getKey();
                AttributeInstance atk = t.getAttribute(Attributes.ATTACK_DAMAGE);
                if (atk != null) atk.removeModifier(ECLIPSE_DEBUFF_ID);
                if (rangedWeaponAvailable) {
                    var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
                    rangedAttr.ifPresent(attr -> {
                        AttributeInstance inst = t.getAttribute(attr);
                        if (inst != null) inst.removeModifier(ECLIPSE_RANGED_DEBUFF_ID);
                    });
                }
                if (spellPowerAvailable) {
                    var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
                    spellAttr.ifPresent(attr -> {
                        AttributeInstance inst = t.getAttribute(attr);
                        if (inst != null) inst.removeModifier(ECLIPSE_SPELL_DEBUFF_ID);
                    });
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        debuffedTargets.forEach((target, expiry) -> {
            AttributeInstance atk = target.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) atk.removeModifier(ECLIPSE_DEBUFF_ID);
            if (rangedWeaponAvailable) {
                var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
                rangedAttr.ifPresent(attr -> {
                    AttributeInstance inst = target.getAttribute(attr);
                    if (inst != null) inst.removeModifier(ECLIPSE_RANGED_DEBUFF_ID);
                });
            }
            if (spellPowerAvailable) {
                var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
                spellAttr.ifPresent(attr -> {
                    AttributeInstance inst = target.getAttribute(attr);
                    if (inst != null) inst.removeModifier(ECLIPSE_SPELL_DEBUFF_ID);
                });
            }
        });
        debuffedTargets.clear();
        AttributeInstance selfAtk = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (selfAtk != null) selfAtk.removeModifier(ECLIPSE_BOOST_ID);
        if (rangedWeaponAvailable) {
            var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
            rangedAttr.ifPresent(attr -> {
                AttributeInstance inst = entity.getAttribute(attr);
                if (inst != null) inst.removeModifier(ECLIPSE_RANGED_BOOST_ID);
            });
        }
        if (spellPowerAvailable) {
            var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
            spellAttr.ifPresent(attr -> {
                AttributeInstance inst = entity.getAttribute(attr);
                if (inst != null) inst.removeModifier(ECLIPSE_SPELL_BOOST_ID);
            });
        }
    }
}
