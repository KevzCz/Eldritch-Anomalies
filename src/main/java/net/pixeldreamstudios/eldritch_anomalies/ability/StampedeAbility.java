package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

public class StampedeAbility implements Ability {

    private final EldritchAnomaliesConfig.StampedeConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.stampedeConfig;

    private long nextUseTime = 0;
    private boolean isCharging = false;
    private Vec3 chargeDirection = null;
    private long chargeEndTime = 0;

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
        if (mob.level().isClientSide || isCharging) return false;
        LivingEntity target = mob.getTarget();
        double dist = target != null ? mob.distanceTo(target) : 0;
        return target != null
                && mob.level().getGameTime() > nextUseTime
                && dist >= config.minRange
                && dist <= config.maxRange;
    }

    @Override
    public void onAbilityUse(Mob mob) {
        if (!canUseAbility(mob)) return;
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        chargeDirection = target.position().subtract(mob.position()).normalize();
        mob.setDeltaMovement(
                chargeDirection.x * config.chargeSpeed,
                0.2,
                chargeDirection.z * config.chargeSpeed);
        mob.hurtMarked = true;

        isCharging = true;
        chargeEndTime = mob.level().getGameTime() + config.chargeDuration;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (!isCharging || mob.level().isClientSide) return;
        long now = mob.level().getGameTime();

        if (now > chargeEndTime || mob.horizontalCollision) {
            isCharging = false;
            chargeDirection = null;
            nextUseTime = now + config.cooldown;
            return;
        }

        mob.setDeltaMovement(
                chargeDirection.x * config.chargeSpeed,
                mob.getDeltaMovement().y,
                chargeDirection.z * config.chargeSpeed);
        mob.hurtMarked = true;

        LivingEntity target = mob.getTarget();
        if (target != null && mob.distanceTo(target) <= config.impactRange) {
            target.hurt(mob.damageSources().mobAttack(mob), config.impactDamage);
            target.setDeltaMovement(
                    chargeDirection.x * config.impactKnockback,
                    0.4,
                    chargeDirection.z * config.impactKnockback);
            target.hurtMarked = true;
            isCharging = false;
            chargeDirection = null;
            nextUseTime = now + config.cooldown;
        }
    }
}
