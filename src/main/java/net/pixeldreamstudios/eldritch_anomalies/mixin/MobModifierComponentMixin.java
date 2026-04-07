package net.pixeldreamstudios.eldritch_anomalies.mixin;

import net.hyper_pigeon.eldritch_mobs.EldritchMobsMod;
import net.hyper_pigeon.eldritch_mobs.component.MobModifierComponent;
import net.hyper_pigeon.eldritch_mobs.rank.MobRank;
import net.hyper_pigeon.eldritch_mobs.register.EldritchMobTagKeys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.pixeldreamstudios.eldritch_anomalies.DynamicTagRegistry;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobModifierComponent.class, remap = false)
public abstract class MobModifierComponentMixin {

    @Shadow
    private MobRank rank;

    @Shadow
    private Mob provider;

    @Shadow
    public abstract void setRank(MobRank rank);

    @Inject(method = "canBeBuffed", at = @At("HEAD"), cancellable = true)
    private void injectDynamicTagCheck(Mob mobEntity, CallbackInfoReturnable<Boolean> cir) {
        if (this.rank != MobRank.UNDECIDED) {
            return;
        }

        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(mobEntity.getType());
        String entityId = entityKey != null ? entityKey.toString() : null;

        if (entityId != null && config.blacklistedMobs.contains(entityId)) {
            cir.setReturnValue(false);
            return;
        }

        boolean hasCustomNameAndIgnored = mobEntity.hasCustomName() && EldritchMobsMod.ELDRITCH_MOBS_CONFIG.ignoreNamedMobs;

        if (entityId != null && !hasCustomNameAndIgnored) {
            if (config.allowedMobs.contains(entityId)
                    || config.alwaysEliteMobs.contains(entityId)
                    || config.alwaysUltraMobs.contains(entityId)
                    || config.alwaysEldritchMobs.contains(entityId)) {
                cir.setReturnValue(true);
                return;
            }
        }

        boolean inAllowedTag = mobEntity.getType().is(EldritchMobTagKeys.ALLOWED);
        boolean inBlacklist = mobEntity.getType().is(EldritchMobTagKeys.BLACKLIST);
        boolean dynamicCheck = DynamicTagRegistry.isEntityAllowed(mobEntity.getType());

        if (!inBlacklist && !hasCustomNameAndIgnored && (inAllowedTag || dynamicCheck)) {
            cir.setReturnValue(true);
        } else if (!inBlacklist && !hasCustomNameAndIgnored && !inAllowedTag) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "randomlySetRank", at = @At("RETURN"))
    private void injectConfigRankOverride(CallbackInfo ci) {
        if (this.provider == null) return;
        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(this.provider.getType());
        if (entityKey == null) return;
        String entityId = entityKey.toString();
        if (config.alwaysEldritchMobs.contains(entityId)) {
            setRank(MobRank.ELDRITCH);
        } else if (config.alwaysUltraMobs.contains(entityId)) {
            setRank(MobRank.ULTRA);
        } else if (config.alwaysEliteMobs.contains(entityId)) {
            setRank(MobRank.ELITE);
        }
    }
}