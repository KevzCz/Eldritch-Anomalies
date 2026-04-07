package net.pixeldreamstudios.eldritch_anomalies.ability;

import net.hyper_pigeon.eldritch_mobs.ability.Ability;
import net.hyper_pigeon.eldritch_mobs.ability.AbilitySubType;
import net.hyper_pigeon.eldritch_mobs.ability.AbilityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;

public class BulwarkAbility implements Ability {

    private final EldritchAnomaliesConfig.BulwarkConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG.bulwarkConfig;
    private final ResourceLocation HEALTH_ID = EldritchAnomalies.id("bulwark_health");
    private final ResourceLocation ARMOR_ID = EldritchAnomalies.id("bulwark_armor");

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
        return AbilitySubType.DEFENSIVE;
    }

    @Override
    public void passiveApply(Mob mob) {
        AttributeInstance healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && !healthAttr.hasModifier(HEALTH_ID)) {
            healthAttr.addPermanentModifier(new AttributeModifier(HEALTH_ID, config.healthMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            mob.setHealth(mob.getMaxHealth());
        }

        AttributeInstance armorAttr = mob.getAttribute(Attributes.ARMOR);
        if (armorAttr != null && !armorAttr.hasModifier(ARMOR_ID)) {
            armorAttr.addPermanentModifier(new AttributeModifier(ARMOR_ID, config.armorBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
