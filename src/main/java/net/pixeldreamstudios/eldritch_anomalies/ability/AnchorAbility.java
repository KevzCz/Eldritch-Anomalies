package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.WeakHashMap;

public class AnchorAbility implements Ability {

    private final EldritchAnomaliesConfig.AnchorConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.anchorConfig;

    private final ResourceLocation KNOCKBACK_ID = EldritchAnomalies.id("anchor_knockback");
    private static final ResourceLocation ANCHOR_SLOW_ID = EldritchAnomalies.id("anchor_slow_debuff");

    private final WeakHashMap<LivingEntity, Long> slowedAttackers = new WeakHashMap<>();

    @Override
    public String getName() { return config.name; }

    @Override
    public AbilityType getAbilityType() { return AbilityType.PASSIVE; }

    @Override
    public AbilitySubType getAbilitySubType() { return AbilitySubType.DEFENSIVE; }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;

        AttributeInstance kbRes = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null && !kbRes.hasModifier(KNOCKBACK_ID)) {
            kbRes.addPermanentModifier(new AttributeModifier(
                    KNOCKBACK_ID, config.knockbackResistance,
                    AttributeModifier.Operation.ADD_VALUE));
        }

        if (mob.getHealth() / mob.getMaxHealth() < config.lowHealthThreshold) {
            if (!mob.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                mob.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE, 40,
                        config.resistanceAmplifier, false, false));
            }
        }

        long now = mob.level().getGameTime();
        slowedAttackers.entrySet().removeIf(entry -> {
            if (now > entry.getValue() || !entry.getKey().isAlive()) {
                AttributeInstance speed = entry.getKey().getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(ANCHOR_SLOW_ID);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDamaged(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Mob mob)) return;
        if (mob.level().isClientSide) return;

        Entity rawAttacker = source.getEntity();
        if (!(rawAttacker instanceof LivingEntity attacker)) return;

        Vec3 pushDir = attacker.position().subtract(mob.position()).normalize();
        attacker.setDeltaMovement(attacker.getDeltaMovement().add(
                pushDir.x * config.retaliationForce,
                0.3,
                pushDir.z * config.retaliationForce));
        attacker.hurtMarked = true;

        AttributeInstance speedAttr = attacker.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(ANCHOR_SLOW_ID);
            speedAttr.addTransientModifier(new AttributeModifier(
                    ANCHOR_SLOW_ID, -config.retaliationSlowAmount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            slowedAttackers.put(attacker, mob.level().getGameTime() + config.retaliationSlowDuration);
        }
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        slowedAttackers.forEach((target, expiry) -> {
            AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(ANCHOR_SLOW_ID);
        });
        slowedAttackers.clear();
    }
}
