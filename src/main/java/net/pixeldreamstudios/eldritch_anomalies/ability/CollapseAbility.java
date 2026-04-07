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

public class CollapseAbility implements Ability {

    private final EldritchAnomaliesConfig.CollapseConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.collapseConfig;
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
    public boolean canUseAbility(Mob mob) {
        if (mob.level().isClientSide) return false;
        LivingEntity target = mob.getTarget();
        return target != null
                && mob.level().getGameTime() > nextUseTime
                && mob.distanceTo(target) <= config.triggerRange;
    }

    @Override
    public void onAbilityUse(Mob mob) {
        if (mob.level().isClientSide || !canUseAbility(mob)) return;

        AABB area = mob.getBoundingBox().inflate(config.radius);
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(
                LivingEntity.class, area,
                e -> e != mob && !e.isAlliedTo(mob));

        for (LivingEntity target : nearby) {
            if (mob.distanceTo(target) > config.radius) continue;
            Vec3 cur = target.getDeltaMovement();
            target.setDeltaMovement(cur.x * 0.2, config.launchForce, cur.z * 0.2);
            target.hurtMarked = true;
            target.hurt(mob.damageSources().mobAttack(mob), config.damage);
        }

        nextUseTime = mob.level().getGameTime() + config.cooldown;
    }
}
