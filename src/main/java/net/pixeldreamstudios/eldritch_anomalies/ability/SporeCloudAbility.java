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

public class SporeCloudAbility implements Ability {

    private final EldritchAnomaliesConfig.SporeCloudConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.sporeCloudConfig;

    private long nextUseTime = 0;
    private final WeakHashMap<LivingEntity, Long> sporedEntities = new WeakHashMap<>();

    private static final ResourceLocation SPORE_SLOW_ID =
            EldritchAnomalies.id("spore_cloud_slow_debuff");

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

        AABB area = mob.getBoundingBox().inflate(config.radius);
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(
                LivingEntity.class, area, e -> e != mob && !e.isAlliedTo(mob));

        for (LivingEntity target : nearby) {
            if (mob.distanceTo(target) > config.radius) continue;

            target.hurt(mob.damageSources().magic(), config.tickDamage * 2);

            AttributeInstance speedAttr = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPORE_SLOW_ID);
                speedAttr.addTransientModifier(new AttributeModifier(
                        SPORE_SLOW_ID, -config.sporeSlowAmount,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            sporedEntities.put(target, now + config.sporeDuration);
        }

        nextUseTime = now + config.cooldown;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        long now = mob.level().getGameTime();

        sporedEntities.entrySet().removeIf(entry -> {
            LivingEntity target = entry.getKey();
            if (now > entry.getValue() || !target.isAlive()) {
                AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(SPORE_SLOW_ID);
                return true;
            }
            if (now % config.tickDamageInterval == 0) {
                target.hurt(mob.damageSources().magic(), config.tickDamage);
            }
            return false;
        });
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        sporedEntities.forEach((target, expiry) -> {
            AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(SPORE_SLOW_ID);
        });
        sporedEntities.clear();
    }
}
