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

public class CorrosiveAbility implements Ability {

    private final EldritchAnomaliesConfig.CorrosiveConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.corrosiveConfig;
    private final WeakHashMap<LivingEntity, Integer> stacks = new WeakHashMap<>();

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
        if (!(attacker instanceof Mob)) return;

        int currentStacks = stacks.getOrDefault(target, 0);
        if (currentStacks >= config.maxStacks) return;

        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor == null) return;

        ResourceLocation modId = EldritchAnomalies.id("corrosive_" + currentStacks + "_debuff");
        if (!armor.hasModifier(modId)) {
            armor.addTransientModifier(new AttributeModifier(modId, -config.armorReductionPerStack, AttributeModifier.Operation.ADD_VALUE));
            stacks.put(target, currentStacks + 1);
        }
    }

    private void removeAllStacks(LivingEntity target, int stackCount) {
        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        for (int i = 0; i < stackCount; i++) {
            armor.removeModifier(EldritchAnomalies.id("corrosive_" + i + "_debuff"));
        }
    }

    @Override
    public void passiveApply(Mob mob) {
        if (mob.level().isClientSide) return;
        stacks.entrySet().removeIf(entry -> {
            if (!entry.getKey().isAlive()) {
                removeAllStacks(entry.getKey(), entry.getValue());
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDeath(LivingEntity entity, DamageSource source) {
        stacks.forEach((target, count) -> removeAllStacks(target, count));
        stacks.clear();
    }
}
