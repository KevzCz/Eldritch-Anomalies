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
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.WeakHashMap;

public class CrusherAbility implements Ability {

    private final EldritchAnomaliesConfig.CrusherConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.crusherConfig;

    private long nextUseTime = 0;
    private final WeakHashMap<LivingEntity, Long> crushedTargets = new WeakHashMap<>();

    private static final ResourceLocation CRUSH_SLOW_ID =
            EldritchAnomalies.id("crusher_slow_debuff");

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
        return mob.level().getGameTime() > nextUseTime && mob.getTarget() != null;
    }

    @Override
    public void onAttack(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof Mob mob)) return;
        if (!canUseAbility(mob)) return;
        long now = mob.level().getGameTime();

        Vec3 knockback = target.position().subtract(attacker.position())
                .normalize().scale(config.knockbackStrength);
        target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.5, knockback.z));
        target.hurtMarked = true;

        AttributeInstance speedAttr = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(CRUSH_SLOW_ID);
            speedAttr.addTransientModifier(new AttributeModifier(
                    CRUSH_SLOW_ID, -config.slowAmount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            crushedTargets.put(target, now + config.slownessDuration);
        }

        nextUseTime = now + config.cooldown;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        long now = mob.level().getGameTime();
        crushedTargets.entrySet().removeIf(entry -> {
            if (now > entry.getValue() || !entry.getKey().isAlive()) {
                AttributeInstance speed = entry.getKey().getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(CRUSH_SLOW_ID);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        crushedTargets.forEach((target, expiry) -> {
            AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(CRUSH_SLOW_ID);
        });
        crushedTargets.clear();
    }
}
