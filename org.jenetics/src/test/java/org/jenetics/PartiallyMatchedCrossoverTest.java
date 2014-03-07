/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics;

import static org.jenetics.TestUtils.newPermutationDoubleGenePopulation;
import static org.jenetics.stat.StatisticsAssert.assertDistribution;
import static org.jenetics.util.factories.Int;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.jenetics.stat.Histogram;
import org.jenetics.stat.NormalDistribution;
import org.jenetics.stat.Variance;
import org.jenetics.util.Array;
import org.jenetics.util.ISeq;
import org.jenetics.util.Range;


/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version <em>$Date: 2014-03-07 $</em>
 */
@SuppressWarnings("deprecation")
public class PartiallyMatchedCrossoverTest {


	@Test(invocationCount = 10)
	public void crossover() {
		final PartiallyMatchedCrossover<Integer> pmco =
			new PartiallyMatchedCrossover<>(1);

		final int length = 1000;
		final Array<Integer> alleles = new Array<Integer>(length).fill(Int());
		final ISeq<Integer> ialleles = alleles.toISeq();

		final Array<EnumGene<Integer>> that = alleles.map(EnumGene.ToGene(ialleles));
		final Array<EnumGene<Integer>> other = alleles.map(EnumGene.ToGene(ialleles));

		that.shuffle();
		other.shuffle();

		final PermutationChromosome<Integer> thatChrom1 = PermutationChromosome.of(that.toISeq());
		Assert.assertTrue(thatChrom1.isValid(), "thatChrom1 not valid");

		final PermutationChromosome<Integer> otherChrom1 = PermutationChromosome.of(other.toISeq());
		Assert.assertTrue(otherChrom1.isValid(), "otherChrom1 not valid");

		pmco.crossover(that, other);

		final PermutationChromosome<Integer> thatChrom2 = PermutationChromosome.of(that.toISeq());
		Assert.assertTrue(thatChrom2.isValid(), "thatChrom2 not valid: " + thatChrom2.toSeq());

		final PermutationChromosome<Integer> otherChrom2 = PermutationChromosome.of(other.toISeq());
		Assert.assertTrue(otherChrom2.isValid(), "otherChrom2 not valid: " + otherChrom2.toSeq());

		Assert.assertFalse(thatChrom1.equals(thatChrom2), "That chromosome must not be equal");
		Assert.assertFalse(otherChrom1.equals(otherChrom2), "That chromosome must not be equal");
	}

	@Test
	public void corssoverWithIllegalChromosome() {
		final PartiallyMatchedCrossover<Integer> pmco = new PartiallyMatchedCrossover<>(1);

		final int length = 1000;
		final Array<Integer> alleles = new Array<Integer>(length).fill(Int());
		final ISeq<Integer> ialleles = alleles.toISeq();

		final Array<EnumGene<Integer>> that = alleles.map(EnumGene.ToGene(ialleles));
		final Array<EnumGene<Integer>> other = alleles.map(EnumGene.ToGene(ialleles));

		pmco.crossover(that, other);

	}

	@Test(dataProvider = "alterProbabilityParameters")
	public void alterProbability(
		final Integer ngenes,
		final Integer nchromosomes,
		final Integer npopulation,
		final Double p
	) {
		final Population<EnumGene<Double>, Double> population = newPermutationDoubleGenePopulation(
				ngenes, nchromosomes, npopulation
			);

		// The mutator to test.
		final PartiallyMatchedCrossover<Double> crossover = new PartiallyMatchedCrossover<>(p);

		final long nallgenes = ngenes*nchromosomes*npopulation;
		final long N = 100;
		final double mean = crossover.getOrder()*npopulation*p;

		final long min = 0;
		final long max = nallgenes;
		final Range<Long> domain = new Range<>(min, max);

		final Histogram<Long> histogram = Histogram.of(min, max, 10);
		final Variance<Long> variance = new Variance<>();

		for (int i = 0; i < N; ++i) {
			final long alterations = crossover.alter(population, 1);
			histogram.accumulate(alterations);
			variance.accumulate(alterations);
		}

		// Normal distribution as approximation for binomial distribution.
		assertDistribution(histogram, new NormalDistribution<>(domain, mean, variance.getVariance()));
	}

	@DataProvider(name = "alterProbabilityParameters")
	public Object[][] alterProbabilityParameters() {
		return TestUtils.alterProbabilityParameters();
	}

}
