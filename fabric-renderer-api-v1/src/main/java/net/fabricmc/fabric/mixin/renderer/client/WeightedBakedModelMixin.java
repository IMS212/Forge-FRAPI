/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.renderer.client;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

@Mixin(WeightedBakedModel.class)
public class WeightedBakedModelMixin implements FabricBakedModel {
	@Shadow
	@Final
	private int totalWeight;
	@Shadow
	@Final
	private List<WeightedEntry.Wrapper<BakedModel>> list;
	@Unique
	boolean isVanilla = true;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void onInit(List<WeightedEntry.Wrapper<BakedModel>> models, CallbackInfo cb) {
		for (WeightedEntry.Wrapper<BakedModel> model : models) {
			if (!((FabricBakedModel) model.data()).isVanillaAdapter()) {
				isVanilla = false;
				break;
			}
		}
	}

	@Override
	public boolean isVanillaAdapter() {
		return isVanilla;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, RenderType renderType, ModelData modelData, Supplier<RandomSource> randomSupplier, RenderContext context) {
		WeightedEntry.Wrapper<BakedModel> selected = WeightedRandom.getWeightedItem(this.list, Math.abs((int) randomSupplier.get().nextLong()) % this.totalWeight).orElse(null);

		if (selected != null) {
			((FabricBakedModel) selected.data()).emitBlockQuads(blockView, state, pos, renderType, modelData, () -> {
				RandomSource random = randomSupplier.get();
				random.nextLong(); // Imitate vanilla modifying the random before passing it to the submodel
				return random;
			}, context);
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		WeightedEntry.Wrapper<BakedModel> selected = WeightedRandom.getWeightedItem(this.list, Math.abs((int) randomSupplier.get().nextLong()) % this.totalWeight).orElse(null);

		if (selected != null) {
			((FabricBakedModel) selected.data()).emitItemQuads(stack, () -> {
				RandomSource random = randomSupplier.get();
				random.nextLong(); // Imitate vanilla modifying the random before passing it to the submodel
				return random;
			}, context);
		}
	}
}
