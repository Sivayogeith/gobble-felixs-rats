package net.sage.gobblerats.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rats.entity.RatEntity;

import java.util.Optional;

@Mixin(CatEntity.class)
public class CatMixin extends PathAwareEntity {
	protected CatMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method = "initGoals")
	private void init(CallbackInfo info) {
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, RatEntity.class, false));
	}

	@Override
	public boolean tryAttack(ServerWorld world, Entity target) {
		float f = (float) this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
		if (target instanceof RatEntity){
			f = (float) 10.0;
		}
		ItemStack itemStack = this.getWeaponStack();
		DamageSource damageSource = (DamageSource) Optional.ofNullable(itemStack.getItem().getDamageSource(this)).orElse(this.getDamageSources().mobAttack(this));
		f = EnchantmentHelper.getDamage(world, itemStack, target, damageSource, f);
		f += itemStack.getItem().getBonusAttackDamage(target, f, damageSource);
		boolean bl = target.damage(world, damageSource, f);
		if (bl) {
			float g = this.getAttackKnockbackAgainst(target, damageSource);
			if (g > 0.0F && target instanceof LivingEntity livingEntity) {
				livingEntity.takeKnockback(g * 0.5F, MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)), -MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0)));
				this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
			}

			if (target instanceof LivingEntity livingEntity) {
				itemStack.postHit(livingEntity, this);
			}

			EnchantmentHelper.onTargetDamaged(world, target, damageSource);
			this.onAttacking(target);
			this.playAttackSound();
		}

		return bl;
	}
}


