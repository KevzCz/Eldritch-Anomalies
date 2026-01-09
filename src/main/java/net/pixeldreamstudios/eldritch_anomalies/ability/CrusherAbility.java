package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

public class CrusherAbility implements Ability {

    private final EldritchAnomaliesConfig.CrusherConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.crusherConfig;
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
    public void onAttack(LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Mob mob && canUseAbility(mob)) {
            Vec3 knockback = target.position().subtract(attacker.position()).normalize().scale(config.knockbackStrength);
            target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.5, knockback.z));
            target.hurtMarked = true;

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.slownessDuration, config.slownessAmplifier));

            nextUseTime = mob.level().getGameTime() + config.cooldown;
        }
    }
}