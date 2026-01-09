package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

public class GiganticAbility implements Ability {

    private final EldritchAnomaliesConfig.GiganticConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.giganticConfig;

    private final AttributeModifier SIZE_MODIFIER = new AttributeModifier(
            EldritchAnomalies.id("gigantic_size"),
            config.sizeMultiplier,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private final AttributeModifier DAMAGE_MODIFIER = new AttributeModifier(
            EldritchAnomalies.id("gigantic_damage"),
            config.damageMultiplier,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

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
    public void passiveApply(Mob mobEntity) {
        AttributeInstance scaleAttr = mobEntity.getAttribute(Attributes.SCALE);
        if (scaleAttr != null && !  scaleAttr.hasModifier(SIZE_MODIFIER.id())) {
            scaleAttr.addPermanentModifier(SIZE_MODIFIER);
        }

        AttributeInstance damageAttr = mobEntity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null && !  damageAttr.hasModifier(DAMAGE_MODIFIER.id())) {
            damageAttr.addPermanentModifier(DAMAGE_MODIFIER);
        }
    }
}