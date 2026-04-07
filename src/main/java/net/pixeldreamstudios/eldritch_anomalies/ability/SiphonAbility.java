package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SiphonAbility implements Ability {

    private final EldritchAnomaliesConfig.SiphonConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.siphonConfig;
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
        return AbilitySubType.DEFENSIVE;
    }

    @Override
    public long getCooldown() {
        return config.cooldown;
    }

    @Override
    public void onDamaged(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        long currentTime = mob.level().getGameTime();
        if (currentTime <= nextUseTime) {
            return;
        }

        float healthPercent = mob.getHealth() / mob.getMaxHealth();
        if (healthPercent > config.healthThreshold) {
            return;
        }

        if (mob.level().isClientSide) {
            return;
        }

        AABB area = mob.getBoundingBox().inflate(config.drainRadius);
        List<LivingEntity> nearbyEntities = mob.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> target != mob && mob.distanceTo(target) <= config.drainRadius
        );

        List<LivingEntity> prioritizedTargets = prioritizeTargets(mob, nearbyEntities);

        float totalHealing = 0.0F;
        int drainedCount = 0;

        for (LivingEntity target : prioritizedTargets) {
            if (drainedCount >= config.maxTargets) {
                break;
            }

            if (! target.isAlive() || target.isAlliedTo(mob)) {
                continue;
            }

            float actualDamage = Math.min(config.drainAmount, target.getHealth() - config.minimumHealthLeft);

            if (actualDamage > 0) {
                target.hurt(mob.damageSources().magic(), actualDamage);
                totalHealing += actualDamage;
                drainedCount++;
            }
        }

        if (totalHealing > 0) {
            mob.heal(totalHealing);
            nextUseTime = currentTime + config.cooldown;
        }
    }

    private List<LivingEntity> prioritizeTargets(Mob mob, List<LivingEntity> entities) {
        List<LivingEntity> prioritized = new ArrayList<>(entities);

        prioritized.sort(Comparator.comparingInt((LivingEntity target) -> {
            if (target instanceof Player) {
                return 0;
            }
            if (target == mob.getTarget() || target == mob.getLastHurtByMob()) {
                return 1;
            }
            if (target instanceof Monster) {
                return 2;
            }
            if (target instanceof Animal) {
                return 3;
            }
            return 4;
        }));

        return prioritized;
    }
}
