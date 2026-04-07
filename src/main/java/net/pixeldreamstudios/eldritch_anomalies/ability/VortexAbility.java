package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.List;

public class VortexAbility implements Ability {

    private final EldritchAnomaliesConfig.VortexConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.vortexConfig;
    private long nextUseTime = 0;

    @Override
    public String getName() {
        return config.name;
    }

    @Override
    public AbilityType getAbilityType() {
        return AbilityType.ACTIVE;
    }

    @Override
    public AbilitySubType getAbilitySubType() {
        return AbilitySubType.OFFENSIVE;
    }

    @Override
    public long getCooldown() {
        return config.cooldown;
    }

    @Override
    public boolean canUseAbility(Mob mobEntity) {
        return mobEntity.level().getGameTime() > nextUseTime && mobEntity.getTarget() != null;
    }

    @Override
    public void onAbilityUse(Mob mobEntity) {
        if (mobEntity.level().isClientSide || !canUseAbility(mobEntity)) {
            return;
        }

        AABB area = mobEntity.getBoundingBox().inflate(config.pullRadius);
        List<LivingEntity> nearbyEntities = mobEntity.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != mobEntity && mobEntity.distanceTo(entity) <= config.pullRadius
        );

        Vec3 vortexCenter = mobEntity.position();

        for (LivingEntity target : nearbyEntities) {
            if (! target.isAlive() || target.isAlliedTo(mobEntity)) {
                continue;
            }

            Vec3 targetPos = target.position();
            Vec3 direction = vortexCenter.subtract(targetPos).normalize();
            double distance = vortexCenter.distanceTo(targetPos);
            double pullForce = config.pullStrength * (1.0 - (distance / config.pullRadius));

            Vec3 pullVelocity = direction.scale(pullForce);

            double verticalBoost = target.onGround() ? config.verticalLiftBoost : 0.0;

            target.setDeltaMovement(
                    target.getDeltaMovement().add(
                            pullVelocity.x,
                            pullVelocity.y + verticalBoost,
                            pullVelocity.z
                    )
            );
            target.hurtMarked = true;

            if (distance < config.closeDamageDistance) {
                target.hurt(mobEntity.damageSources().mobAttack(mobEntity), config.damage);
            }
        }

        nextUseTime = mobEntity.level().getGameTime() + config.cooldown;
    }
}
