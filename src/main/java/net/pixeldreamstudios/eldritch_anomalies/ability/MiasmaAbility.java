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

public class MiasmaAbility implements Ability {

    private final EldritchAnomaliesConfig.MiasmaConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.miasmaConfig;

    private final WeakHashMap<LivingEntity, Long> slowedEntities = new WeakHashMap<>();

    private static final ResourceLocation MIASMA_SLOW_ID =
            EldritchAnomalies.id("miasma_slow_debuff");

    @Override
    public String getName() { return config.name; }

    @Override
    public AbilityType getAbilityType() { return AbilityType.PASSIVE; }

    @Override
    public AbilitySubType getAbilitySubType() { return AbilitySubType.OFFENSIVE; }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        long now = mob.level().getGameTime();

        slowedEntities.entrySet().removeIf(entry -> {
            if (now > entry.getValue() || !entry.getKey().isAlive()) {
                AttributeInstance speed = entry.getKey().getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(MIASMA_SLOW_ID);
                return true;
            }
            return false;
        });

        if (now % config.pulseInterval != 0) return;

        AABB area = mob.getBoundingBox().inflate(config.radius);
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(
                LivingEntity.class, area, e -> e != mob && !e.isAlliedTo(mob));

        for (LivingEntity target : nearby) {
            if (mob.distanceTo(target) > config.radius) continue;
            target.hurt(mob.damageSources().magic(), config.damage);

            AttributeInstance speedAttr = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(MIASMA_SLOW_ID);
                speedAttr.addTransientModifier(new AttributeModifier(
                        MIASMA_SLOW_ID, -config.miasmaSlowAmount,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
slowedEntities.put(target, now + config.nauseaDuration);
            }
        }
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        slowedEntities.forEach((target, expiry) -> {
            AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(MIASMA_SLOW_ID);
        });
        slowedEntities.clear();
    }
}
