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
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.WeakHashMap;

public class FractureAbility implements Ability {

    private final EldritchAnomaliesConfig.FractureConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.fractureConfig;
    private static final ResourceLocation FRACTURE_ID = EldritchAnomalies.id("fracture_max_health_debuff");
    private long nextUseTime = 0;
    private final WeakHashMap<LivingEntity, Boolean> fracturedTargets = new WeakHashMap<>();

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
    public void onAttack(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof Mob mob)) return;
        long now = mob.level().getGameTime();
        if (now <= nextUseTime) return;

        AttributeInstance attr = target.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;

        if (!attr.hasModifier(FRACTURE_ID)) {
            attr.addTransientModifier(new AttributeModifier(FRACTURE_ID, -config.healthReduction, AttributeModifier.Operation.ADD_VALUE));
            fracturedTargets.put(target, true);
        }
        if (target.getHealth() > target.getMaxHealth()) {
            target.setHealth(target.getMaxHealth());
        }

        nextUseTime = now + config.cooldown;
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        fracturedTargets.entrySet().removeIf(entry -> {
            if (!entry.getKey().isAlive()) {
                AttributeInstance attr = entry.getKey().getAttribute(Attributes.MAX_HEALTH);
                if (attr != null) attr.removeModifier(FRACTURE_ID);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        fracturedTargets.forEach((target, ignored) -> {
            AttributeInstance attr = target.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) attr.removeModifier(FRACTURE_ID);
        });
        fracturedTargets.clear();
    }
}
