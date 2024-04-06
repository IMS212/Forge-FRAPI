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

import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.math.Constants;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SheetedDecalTextureGenerator.class)
public class OverlayVertexConsumerMixin {
	@Unique
	private static final Direction[] DIRECTIONS = Direction.values();

	/*
	The original method call is used to get the closest axis-aligned direction of the world-space
	normal vector for a certain face. The world-space normal vector is computed using matrices
	that change when the camera values change. Due to precision errors during matrix
	multiplication, the computed world-space normal of a face will not remain constant, so the
	closest axis-aligned direction may flicker. This issue only affects faces that are directly
	between two axis-aligned directions (45 degree faces) or three axis-aligned directions.

	The fix involves requiring the dot product of each axis-aligned direction to be a small
	amount greater than the previous maximum dot product to be set as the new maximum.
	
	Optional, as it is already implemented by later Forge versions via MinecraftForge/MinecraftForge#9669
	 */
	@Redirect(method = "endVertex", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getNearestStable(FFF)Lnet/minecraft/core/Direction;"), require = 0)
	private Direction redirectGetFacing(float x, float y, float z) {
		Direction closestDir = Direction.NORTH;
		float maxDot = 1.4E-45F;

		for (Direction direction : DIRECTIONS) {
			float dot = x * direction.getStepX() + y * direction.getStepY() + z * direction.getStepZ();

			if (dot > maxDot + Constants.EPSILON) {
				maxDot = dot;
				closestDir = direction;
			}
		}

		return closestDir;
	}
}
