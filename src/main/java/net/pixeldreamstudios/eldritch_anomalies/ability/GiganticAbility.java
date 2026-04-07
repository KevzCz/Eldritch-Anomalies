package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.fabricmc.loader.api.FabricLoader;
import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

import java.util.List;

public class GiganticAbility implements Ability {

    private final EldritchAnomaliesConfig.GiganticConfig config =
            EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.giganticConfig;

    private static final boolean rangedWeaponAvailable = FabricLoader.getInstance().isModLoaded("ranged_weapon");
    private static final boolean spellPowerAvailable = FabricLoader.getInstance().isModLoaded("spell_power");

    private static final ResourceLocation GIGANTIC_RANGED_ID = EldritchAnomalies.id("gigantic_ranged_damage");
    private static final ResourceLocation GIGANTIC_SPELL_ID = EldritchAnomalies.id("gigantic_spell_damage");

    private final AttributeModifier SIZE_MODIFIER = new AttributeModifier(
            EldritchAnomalies.id("gigantic_size"),
            config.sizeMultiplier,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    private final AttributeModifier DAMAGE_MODIFIER = new AttributeModifier(
            EldritchAnomalies.id("gigantic_damage"),
            config.damageMultiplier,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    @Override
    public String getName() { return config.name; }

    @Override
    public AbilityType getAbilityType() { return AbilityType.PASSIVE; }

    @Override
    public AbilitySubType getAbilitySubType() { return AbilitySubType.OFFENSIVE; }

    @Override
    public void passiveApply(Mob mob) {
        AttributeInstance scaleAttr = mob.getAttribute(Attributes.SCALE);
        if (scaleAttr != null && !scaleAttr.hasModifier(SIZE_MODIFIER.id())) {
            scaleAttr.addPermanentModifier(SIZE_MODIFIER);
        }
        AttributeInstance damageAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null && !damageAttr.hasModifier(DAMAGE_MODIFIER.id())) {
            damageAttr.addPermanentModifier(DAMAGE_MODIFIER);
        }
        if (rangedWeaponAvailable) {
            var rangedAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("ranged_weapon:damage"));
            rangedAttr.ifPresent(attr -> {
                AttributeInstance inst = mob.getAttribute(attr);
                if (inst != null && !inst.hasModifier(GIGANTIC_RANGED_ID)) {
                    inst.addPermanentModifier(new AttributeModifier(
                            GIGANTIC_RANGED_ID, config.damageMultiplier,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            });
        }
        if (spellPowerAvailable) {
            var spellAttr = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("spell_power:generic"));
            spellAttr.ifPresent(attr -> {
                AttributeInstance inst = mob.getAttribute(attr);
                if (inst != null && !inst.hasModifier(GIGANTIC_SPELL_ID)) {
                    inst.addPermanentModifier(new AttributeModifier(
                            GIGANTIC_SPELL_ID, config.damageMultiplier,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            });
        }

        if (mob.level().isClientSide) return;
        if (mob.level().getGameTime() % config.tremorInterval != 0) return;
        if (mob.getDeltaMovement().horizontalDistanceSqr() < 0.005) return;

        AABB area = mob.getBoundingBox().inflate(config.tremorRadius);
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(
                LivingEntity.class, area, e -> e != mob && !e.isAlliedTo(mob));

        for (LivingEntity target : nearby) {
            double dist = mob.distanceTo(target);
            if (dist > config.tremorRadius) continue;
            double force = config.tremorForce * (1.0 - dist / config.tremorRadius);
            Vec3 dir = target.position().subtract(mob.position()).normalize();
            target.setDeltaMovement(
                    target.getDeltaMovement().add(dir.x * force, 0.15, dir.z * force));
            target.hurtMarked = true;
        }
    }
}
