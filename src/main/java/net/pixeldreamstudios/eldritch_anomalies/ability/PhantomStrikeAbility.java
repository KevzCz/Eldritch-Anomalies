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

public class PhantomStrikeAbility implements Ability {

    private final EldritchAnomaliesConfig.PhantomStrikeConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.phantomStrikeConfig;

    private long nextStrikeTime = 0;

    @Override
    public String getName() { return config.name; }

    @Override
    public AbilityType getAbilityType() { return AbilityType.PASSIVE; }

    @Override
    public AbilitySubType getAbilitySubType() { return AbilitySubType.DEFENSIVE; }

    @Override
    public void onAttack(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof Mob mob)) return;
        if (mob.level().isClientSide) return;
        long now = mob.level().getGameTime();
        if (now <= nextStrikeTime) return;

        Vec3 behind = target.position().add(
                target.getLookAngle().scale(-config.strikeTeleportDistance));
        mob.teleportTo(behind.x, target.getY(), behind.z);

        mob.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, config.invisibilityDuration, 0, false, false));

        nextStrikeTime = now + config.strikeCooldown;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (!mob.level().isClientSide
                && mob.level().getGameTime() % config.flickerInterval == 0) {
            mob.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY, config.invisibilityDuration, 0, false, false));
        }
    }
}
