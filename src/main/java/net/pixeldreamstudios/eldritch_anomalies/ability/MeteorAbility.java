package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.fabricmc.loader.api.FabricLoader;
import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

public class MeteorAbility implements Ability {

    private final EldritchAnomaliesConfig.MeteorConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.meteorConfig;
    private long nextUseTime = 0;
    private static final ResourceLocation METEOR_SPELL_ID = ResourceLocation.parse("wizards:fire_meteor");
    private static final boolean wizardsModAvailable = FabricLoader.getInstance().isModLoaded("wizards");
    private static Holder<Spell> meteorSpell = null;

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
    public void passiveApply(Mob mobEntity) {
        if (! wizardsModAvailable) {
            return;
        }

        try {
            SpellSchool fireSchool = SpellSchools.FIRE;
            if (fireSchool != null && fireSchool.attributeEntry != null) {
                AttributeInstance firePowerAttr = mobEntity.getAttribute(fireSchool.attributeEntry);
                ResourceLocation modifierId = EldritchAnomalies.id("meteor_fire_power");

                if (firePowerAttr != null && ! firePowerAttr.hasModifier(modifierId)) {
                    AttributeModifier modifier = new AttributeModifier(
                            modifierId,
                            config.firePowerBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    );
                    firePowerAttr.addPermanentModifier(modifier);
                }
            }
        } catch (Exception e) {
            EldritchAnomalies.LOGGER.error("Failed to apply fire power modifier", e);
        }
    }

    @Override
    public boolean canUseAbility(Mob mobEntity) {
        if (!wizardsModAvailable) {
            return false;
        }

        long currentTime = mobEntity.level().getGameTime();
        if (currentTime <= nextUseTime) {
            return false;
        }

        LivingEntity target = mobEntity.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        float distanceToTarget = mobEntity.distanceTo(target);
        if (distanceToTarget > config.castRange) {
            return false;
        }

        return mobEntity.hasLineOfSight(target);
    }

    @Override
    public void onAbilityUse(Mob mobEntity) {
        if (mobEntity.level().isClientSide) {
            return;
        }

        LivingEntity target = mobEntity.getTarget();
        if (target == null || !canUseAbility(mobEntity)) {
            return;
        }

        long time = mobEntity.level().getGameTime();
        if (time <= nextUseTime) {
            return;
        }

        try {
            if (meteorSpell == null && mobEntity.level() instanceof ServerLevel) {
                meteorSpell = SpellRegistry.from(mobEntity.level())
                        .getHolder(METEOR_SPELL_ID)
                        .orElse(null);

                if (meteorSpell == null) {
                    EldritchAnomalies.LOGGER.error("Failed to find meteor spell:  {}", METEOR_SPELL_ID);
                    return;
                }
            }

            if (meteorSpell != null) {
                Spell spell = meteorSpell.value();
                Vec3 targetPos = target.position();
                float distance = (float) mobEntity.position().distanceTo(targetPos);

                SpellHelper.ImpactContext context = new SpellHelper.ImpactContext(
                        1.0F,
                        distance / config.castRange,
                        targetPos,
                        SpellPower.getSpellPower(spell.school, mobEntity),
                        SpellHelper.focusMode(spell),
                        0
                );

                boolean success = SpellHelper.fallProjectile(
                        mobEntity.level(),
                        mobEntity,
                        target,
                        targetPos,
                        meteorSpell,
                        context,
                        0
                );

                if (success) {
                    nextUseTime = time + config.cooldown;
                }
            }
        } catch (Exception e) {
            EldritchAnomalies.LOGGER.error("Failed to cast meteor spell from mob {}",
                    mobEntity.getName().getString(), e);
        }
    }

    public static boolean isAvailable() {
        return wizardsModAvailable;
    }
}
