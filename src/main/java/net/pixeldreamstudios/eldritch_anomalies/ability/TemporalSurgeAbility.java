package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
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

public class TemporalSurgeAbility implements Ability {

    private final EldritchAnomaliesConfig.TemporalSurgeConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.temporalSurgeConfig;

    private long nextUseTime = 0;
    private long surgeExpiry = 0;
    private final WeakHashMap<LivingEntity, Long> slowedTargets = new WeakHashMap<>();

    private static final ResourceLocation SURGE_SPEED_ID =
            EldritchAnomalies.id("temporal_surge_speed");
    private static final ResourceLocation SURGE_SLOW_ID =
            EldritchAnomalies.id("temporal_surge_slow_debuff");

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

        AttributeInstance selfSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (selfSpeed != null && !selfSpeed.hasModifier(SURGE_SPEED_ID)) {
            selfSpeed.addTransientModifier(new AttributeModifier(
                    SURGE_SPEED_ID, config.speedBoost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        surgeExpiry = now + config.duration;

        AABB area = mob.getBoundingBox().inflate(config.radius);
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(
                LivingEntity.class, area, e -> e != mob && !e.isAlliedTo(mob));

        for (LivingEntity target : nearby) {
            if (mob.distanceTo(target) > config.radius) continue;
            AttributeInstance targetSpeed = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (targetSpeed == null) continue;
            targetSpeed.removeModifier(SURGE_SLOW_ID);
            targetSpeed.addTransientModifier(new AttributeModifier(
                    SURGE_SLOW_ID, -config.slowAmount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            slowedTargets.put(target, now + config.duration);
        }

        nextUseTime = now + config.cooldown;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        long now = mob.level().getGameTime();

        if (surgeExpiry > 0 && now > surgeExpiry) {
            AttributeInstance selfSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (selfSpeed != null) selfSpeed.removeModifier(SURGE_SPEED_ID);
            surgeExpiry = 0;
        }

        slowedTargets.entrySet().removeIf(entry -> {
            if (now > entry.getValue() || !entry.getKey().isAlive()) {
                AttributeInstance speed = entry.getKey().getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(SURGE_SLOW_ID);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        slowedTargets.forEach((target, expiry) -> {
            AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(SURGE_SLOW_ID);
        });
        slowedTargets.clear();
        AttributeInstance selfSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (selfSpeed != null) selfSpeed.removeModifier(SURGE_SPEED_ID);
    }
}
