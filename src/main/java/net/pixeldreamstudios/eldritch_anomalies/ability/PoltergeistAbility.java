package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.lang.ref.WeakReference;

public class PoltergeistAbility implements Ability {

    private final EldritchAnomaliesConfig.PoltergeistConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.poltergeistConfig;

    private enum Phase { IDLE, LIFTING, HOLDING, THROWING }

    private Phase phase = Phase.IDLE;
    private int phaseTick = 0;
    private WeakReference<LivingEntity> heldTarget = null;
    private long nextUseTime = 0;

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
        return phase == Phase.IDLE
                && !mob.level().isClientSide
                && mob.level().getGameTime() > nextUseTime
                && mob.getTarget() != null
                && mob.distanceTo(mob.getTarget()) <= config.maxRange;
    }

    @Override
    public void onAbilityUse(Mob mob) {
        if (!canUseAbility(mob)) return;
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        heldTarget = new WeakReference<>(target);
        phase = Phase.LIFTING;
        phaseTick = config.liftTicks;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide || phase == Phase.IDLE) return;

        LivingEntity target = heldTarget != null ? heldTarget.get() : null;
        if (target == null || !target.isAlive()) {
            phase = Phase.IDLE;
            nextUseTime = mob.level().getGameTime() + config.cooldown;
            return;
        }

        switch (phase) {
            case LIFTING -> {
                Vec3 toMob = mob.position().subtract(target.position()).normalize();
                target.setDeltaMovement(toMob.x * 0.15, 0.28, toMob.z * 0.15);
                target.hurtMarked = true;
                target.fallDistance = 0;
                if (--phaseTick <= 0) {
                    phase = Phase.HOLDING;
                    phaseTick = config.holdTicks;
                }
            }
            case HOLDING -> {
                target.setDeltaMovement(
                        target.getDeltaMovement().x * 0.4,
                        0.06,
                        target.getDeltaMovement().z * 0.4);
                target.hurtMarked = true;
                target.fallDistance = 0;
                if (--phaseTick <= 0) {
                    phase = Phase.THROWING;
                }
            }
            case THROWING -> {
                Vec3 throwDir = target.position().subtract(mob.position()).normalize();
                target.setDeltaMovement(
                        throwDir.x * config.horizontalForce,
                        config.verticalForce,
                        throwDir.z * config.horizontalForce);
                target.hurtMarked = true;
                phase = Phase.IDLE;
                nextUseTime = mob.level().getGameTime() + config.cooldown;
            }
            default -> {}
        }
    }
}
