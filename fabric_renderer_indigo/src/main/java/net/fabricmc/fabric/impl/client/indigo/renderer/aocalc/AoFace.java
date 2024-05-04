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

package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import static net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoVertexClampFunction.CLAMP_FUNC;
import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

import net.minecraft.Util;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;

/**
 * Adapted from vanilla BlockModelRenderer.AoCalculator.
 */
enum AoFace {
	AOF_DOWN(new Direction[] { WEST, EAST, NORTH, SOUTH }, (q, i) -> CLAMP_FUNC.clamp(q.y(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.x(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = (1 - u) * v;
		w[1] = (1 - u) * (1 - v);
		w[2] = u * (1 - v);
		w[3] = u * v;
	}),
	AOF_UP(new Direction[] { EAST, WEST, NORTH, SOUTH }, (q, i) -> 1 - CLAMP_FUNC.clamp(q.y(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.x(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = u * v;
		w[1] = u * (1 - v);
		w[2] = (1 - u) * (1 - v);
		w[3] = (1 - u) * v;
	}),
	AOF_NORTH(new Direction[] { UP, DOWN, EAST, WEST }, (q, i) -> CLAMP_FUNC.clamp(q.z(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.x(i));
		w[0] = u * (1 - v);
		w[1] = u * v;
		w[2] = (1 - u) * v;
		w[3] = (1 - u) * (1 - v);
	}),
	AOF_SOUTH(new Direction[] { WEST, EAST, DOWN, UP }, (q, i) -> 1 - CLAMP_FUNC.clamp(q.z(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.x(i));
		w[0] = u * (1 - v);
		w[1] = (1 - u) * (1 - v);
		w[2] = (1 - u) * v;
		w[3] = u * v;
	}),
	AOF_WEST(new Direction[] { UP, DOWN, NORTH, SOUTH }, (q, i) -> CLAMP_FUNC.clamp(q.x(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = u * v;
		w[1] = u * (1 - v);
		w[2] = (1 - u) * (1 - v);
		w[3] = (1 - u) * v;
	}),
	AOF_EAST(new Direction[] { DOWN, UP, NORTH, SOUTH }, (q, i) -> 1 - CLAMP_FUNC.clamp(q.x(i)), (q, i, w) -> {
		final float u = CLAMP_FUNC.clamp(q.y(i));
		final float v = CLAMP_FUNC.clamp(q.z(i));
		w[0] = (1 - u) * v;
		w[1] = (1 - u) * (1 - v);
		w[2] = u * (1 - v);
		w[3] = u * v;
	});

	final Direction[] neighbors;
	final WeightFunction weightFunc;
	final Vertex2Float depthFunc;

	AoFace(Direction[] faces, Vertex2Float depthFunc, WeightFunction weightFunc) {
		this.neighbors = faces;
		this.depthFunc = depthFunc;
		this.weightFunc = weightFunc;
	}

	private static final AoFace[] values = Util.make(new AoFace[6], (neighborData) -> {
		neighborData[DOWN.get3DDataValue()] = AOF_DOWN;
		neighborData[UP.get3DDataValue()] = AOF_UP;
		neighborData[NORTH.get3DDataValue()] = AOF_NORTH;
		neighborData[SOUTH.get3DDataValue()] = AOF_SOUTH;
		neighborData[WEST.get3DDataValue()] = AOF_WEST;
		neighborData[EAST.get3DDataValue()] = AOF_EAST;
	});

	public static AoFace get(Direction direction) {
		return values[direction.get3DDataValue()];
	}

	/**
	 * Implementations handle bilinear interpolation of a point on a light face
	 * by computing weights for each corner of the light face. Relies on the fact
	 * that each face is a unit cube. Uses coordinates from axes orthogonal to face
	 * as distance from the edge of the cube, flipping as needed. Multiplying distance
	 * coordinate pairs together gives sub-area that are the corner weights.
	 * Weights sum to 1 because it is a unit cube. Values are stored in the provided array.
	 */
	@FunctionalInterface
	interface WeightFunction {
		void apply(QuadViewImpl q, int vertexIndex, float[] out);
	}

	@FunctionalInterface
	interface Vertex2Float {
		float apply(QuadViewImpl q, int vertexIndex);
	}
}
