package com.moigferdsrte.fluidloggable.mixin.placement;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item {
    @Shadow
    @Final
    protected Fluid content;

    protected BucketItemMixin(final Properties properties) {
        super(properties);
    }

    @Shadow
    public abstract ClipContext.Fluid getFluidContext();

    @Shadow
    public abstract void checkExtraContent(LivingEntity user, Level level, ItemStack itemStack, BlockPos pos);

    @Shadow
    protected abstract void playEmptySound(LivingEntity user, LevelAccessor level, BlockPos pos);

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void fluidloggable$fillExistingBlock(
            final Level level,
            final Player player,
            final InteractionHand hand,
            final CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (this.content == Fluids.EMPTY && fluidloggable$tryPickupStoredFluid(level, player, hand, cir)) {
            return;
        }

        if (this.content != Fluids.WATER && this.content != Fluids.LAVA) {
            return;
        }

        final ItemStack stack = player.getItemInHand(hand);
        final BlockHitResult hit = getPlayerPOVHitResult(level, player, this.getFluidContext());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        final BlockPos pos = hit.getBlockPos();
        final Direction direction = hit.getDirection();
        if (!level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos.relative(direction), direction, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        final BlockState state = level.getBlockState(pos);
		if (!FluidloggedBlockStateSupport.canStoreFluid(state, this.content)) {
            return;
        }
		if (this.content == Fluids.WATER && LavaloggableBlockSupport.isLavalogged(state)) {
			cir.setReturnValue(InteractionResult.FAIL);
			return;
		}
		if (this.content == Fluids.WATER && state.getBlock() instanceof LiquidBlockContainer) {
			return;
		}
		if (!FluidloggedBlockStateSupport.canPlaceFluid(state, this.content)) {
			return;
		}

		if (this.content == Fluids.WATER
				&& level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) {
            fluidloggable$evaporateWater(level, player, pos);
        } else {
            ((LevelExtension) level).fluidloggable$setFluid(
                    pos,
					this.content == Fluids.LAVA
							? Fluids.LAVA.getSource(false)
							: Fluids.WATER.getSource(false),
                    Block.UPDATE_ALL | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK
            );
            this.playEmptySound(player, level, pos);
        }

        this.checkExtraContent(player, level, stack, pos);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, stack);
        }
        player.awardStat(Stats.ITEM_USED.get((BucketItem) (Object) this));

        final ItemStack result = ItemUtils.createFilledResult(
                stack,
                player,
                BucketItem.getEmptySuccessItem(stack, player)
        );
        cir.setReturnValue(InteractionResult.SUCCESS.heldItemTransformedTo(result));
    }

    @Unique
    private boolean fluidloggable$tryPickupStoredFluid(
            final Level level,
            final Player player,
            final InteractionHand hand,
            final CallbackInfoReturnable<InteractionResult> cir
    ) {
        final ItemStack stack = player.getItemInHand(hand);
        final BlockHitResult hit = getPlayerPOVHitResult(level, player, this.getFluidContext());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        final BlockPos pos = hit.getBlockPos();
        final Direction direction = hit.getDirection();
        if (!level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos.relative(direction), direction, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return true;
        }

        final BlockState state = level.getBlockState(pos);
        final FluidState fluidState = level.getFluidState(pos);
		final Fluid storedFluid = fluidState.getType();
		if ((!fluidState.is(FluidTags.WATER) && !fluidState.is(FluidTags.LAVA))
				|| !FluidloggedBlockStateSupport.containsFluid(state, storedFluid)) {
            return false;
        }

        if (!fluidState.isSource()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return true;
        }

        ((LevelExtension) level).fluidloggable$setFluid(pos, Fluids.EMPTY.defaultFluidState(), Block.UPDATE_ALL);
        final BlockState currentState = level.getBlockState(pos);
		final BlockState dryState = FluidloggedBlockStateSupport.defaultToDry(currentState);
		if (dryState != currentState) {
			level.setBlock(pos, dryState, Block.UPDATE_ALL);
		}
		if (!dryState.canSurvive(level, pos)) {
                level.destroyBlock(pos, true);
        }

        player.awardStat(Stats.ITEM_USED.get((BucketItem) (Object) this));
		storedFluid.getPickupSound().ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

		final ItemStack filledBucket = new ItemStack(
				fluidState.is(FluidTags.LAVA) ? Items.LAVA_BUCKET : Items.WATER_BUCKET
		);
		final ItemStack result = ItemUtils.createFilledResult(stack, player, filledBucket);
        if (!level.isClientSide()) {
			CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, filledBucket);
        }

        cir.setReturnValue(InteractionResult.SUCCESS.heldItemTransformedTo(result));
        return true;
    }

    @Unique
    private static void fluidloggable$evaporateWater(final Level level, final Player player, final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final RandomSource random = level.getRandom();
        level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);

        for (int index = 0; index < 8; index++) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }
}
