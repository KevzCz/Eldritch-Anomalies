package net.pixeldreamstudios.eldritch_anomalies.mixin;

import net.hyper_pigeon.eldritch_mobs.EldritchMobsMod;
import net.hyper_pigeon.eldritch_mobs.component.MobModifierComponent;
import net.hyper_pigeon.eldritch_mobs.rank.MobRank;
import net.hyper_pigeon.eldritch_mobs.register.EldritchMobTagKeys;
import net.minecraft.world.entity.Mob;
import net.pixeldreamstudios.eldritch_anomalies.DynamicTagRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobModifierComponent.class, remap = false)
public abstract class MobModifierComponentMixin {

    @Shadow
    private MobRank rank;

    @Inject(method = "canBeBuffed", at = @At("HEAD"), cancellable = true)
    private void injectDynamicTagCheck(Mob mobEntity, CallbackInfoReturnable<Boolean> cir) {
        if (this.rank != MobRank.UNDECIDED) {
            return;
        }

        boolean inAllowedTag = mobEntity.getType().is(EldritchMobTagKeys.ALLOWED);
        boolean inBlacklist = mobEntity.getType().is(EldritchMobTagKeys.BLACKLIST);
        boolean hasCustomNameAndIgnored = mobEntity.hasCustomName() && EldritchMobsMod.ELDRITCH_MOBS_CONFIG.ignoreNamedMobs;

        boolean dynamicCheck = DynamicTagRegistry.isEntityAllowed(mobEntity.getType());

        if (! inBlacklist && !hasCustomNameAndIgnored && (inAllowedTag || dynamicCheck)) {
            cir.setReturnValue(true);
        } else if (! inBlacklist && ! hasCustomNameAndIgnored && !inAllowedTag) {
            cir.setReturnValue(false);
        }
    }
}