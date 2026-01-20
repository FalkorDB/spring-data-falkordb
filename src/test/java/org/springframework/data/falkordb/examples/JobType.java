/*
 * Copyright (c) 2023-2025 FalkorDB Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.springframework.data.falkordb.examples;

/**
 * Example enum representing different types of employment.
 * 
 * This enum demonstrates Spring Data FalkorDB's automatic enum conversion:
 * - When saving: JobType.FULL_TIME is stored as the string "FULL_TIME"
 * - When reading: The string "FULL_TIME" is converted back to JobType.FULL_TIME
 * 
 * This behavior is compatible with Spring Data Neo4j's enum handling.
 *
 * @author FalkorDB
 * @since 1.0
 */
public enum JobType {
	/**
	 * Full-time employment.
	 */
	FULL_TIME,
	
	/**
	 * Part-time employment.
	 */
	PART_TIME,
	
	/**
	 * Contract-based employment.
	 */
	CONTRACT,
	
	/**
	 * Internship position.
	 */
	INTERN,
	
	/**
	 * Freelance/consultant work.
	 */
	FREELANCE
}
