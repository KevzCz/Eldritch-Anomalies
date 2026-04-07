package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.List;

public class VoidFuryAbility implements Ability {

    private final EldritchAnomaliesConfig.VoidFuryConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.voidFuryConfig;

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
        return AbilitySubType.OFFENSIVE;
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        if (entity.level().isClientSide) return;

        AABB area = entity.getBoundingBox().inflate(config.radius);
        List<LivingEntity> nearby = entity.level().getEntitiesOfClass(
                LivingEntity.class, area,
                e -> e != entity && !e.isAlliedTo(entity));

        for (LivingEntity target : nearby) {
            double dist = entity.distanceTo(target);
            if (dist > config.radius) continue;
            float falloff = (float) (1.0 - dist / config.radius);
            target.hurt(entity.damageSources().magic(), config.damage * falloff);
        }
    }
}
