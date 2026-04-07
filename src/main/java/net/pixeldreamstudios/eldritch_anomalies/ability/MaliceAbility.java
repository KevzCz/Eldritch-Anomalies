package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

public class MaliceAbility implements Ability {

    private final EldritchAnomaliesConfig.MaliceConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.maliceConfig;

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
    public void onDamageToTarget(LivingEntity attacker, LivingEntity target, DamageSource source, float amount) {
        if (!(attacker instanceof Mob mob)) return;

        float missingHealth = target.getMaxHealth() - target.getHealth();
        if (missingHealth <= 0) return;

        float bonus = missingHealth * (float) config.missingHealthPercent;
        if (bonus >= config.minimumBonusDamage) {
            target.hurt(mob.damageSources().magic(), bonus);
        }
    }
}
