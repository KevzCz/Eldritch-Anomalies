package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.WeakHashMap;

public class CursedTouchAbility implements Ability {

    private final EldritchAnomaliesConfig.CursedTouchConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.cursedTouchConfig;

    private final WeakHashMap<LivingEntity, Integer> curseStacks = new WeakHashMap<>();
    private final WeakHashMap<LivingEntity, Long> curseExpiry = new WeakHashMap<>();

    @Override
    public String getName() { return config.name; }

    @Override
    public AbilityType getAbilityType() { return AbilityType.PASSIVE; }

    @Override
    public AbilitySubType getAbilitySubType() { return AbilitySubType.OFFENSIVE; }

    @Override
    public void onDamageToTarget(LivingEntity attacker, LivingEntity target,
                                  DamageSource source, float amount) {
        if (!(attacker instanceof Mob mob)) return;
        long now = mob.level().getGameTime();

        Long expiry = curseExpiry.get(target);
        if (expiry != null && now > expiry) {
            curseStacks.remove(target);
            curseExpiry.remove(target);
        }

        int stacks = curseStacks.getOrDefault(target, 0);

        if (stacks > 0) {
            target.hurt(mob.damageSources().magic(), stacks * config.bonusDamagePerStack);
        }

        curseStacks.put(target, Math.min(stacks + 1, config.maxStacks));
        curseExpiry.put(target, now + config.stackDuration);

        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, config.glowDuration, 0, false, false));
    }
}
