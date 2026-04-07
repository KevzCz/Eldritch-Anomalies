package net.pixeldreamstudios.eldritch_anomalies.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_engine.internals.target.SpellTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRelations.class, remap = false)
public class EntityRelationsMixin {

    @Inject(method = "actionAllowed", at = @At("RETURN"), cancellable = true)
    private static void allowMobSpellTargeting(
            SpellTarget.FocusMode focusMode,
            SpellTarget.Intent intent,
            LivingEntity attacker,
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(attacker instanceof Mob mob)) {
            return;
        }

        if (intent != SpellTarget.Intent.HARMFUL) {
            return;
        }

        if (attacker == target) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (mob.getTarget() == target) {
            cir.setReturnValue(true);
            return;
        }

        if (mob.getLastHurtByMob() == target) {
            cir.setReturnValue(true);
            return;
        }

        if (focusMode == SpellTarget.FocusMode.AREA) {
            if (mob.getTarget() != target && mob.getLastHurtByMob() != target) {
                cir.setReturnValue(false);
            }
        }
    }
}