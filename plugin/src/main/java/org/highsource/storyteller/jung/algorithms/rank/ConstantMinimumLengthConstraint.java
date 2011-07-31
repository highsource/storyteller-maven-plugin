package org.highsource.storyteller.jung.algorithms.rank;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;

public class ConstantMinimumLengthConstraint<E> implements
		Transformer<E, Integer> {

	private final int minimumLength;

	public ConstantMinimumLengthConstraint(int minimumLength) {
		Validate.isTrue(minimumLength >= 0);
		this.minimumLength = minimumLength;
	}

	@Override
	public Integer transform(E edge) {
		return this.minimumLength;
	}

	@SuppressWarnings("rawtypes")
	private static final Transformer ZERO = new ConstantMinimumLengthConstraint(
			0);
	@SuppressWarnings("rawtypes")
	private static final Transformer ONE = new ConstantMinimumLengthConstraint(
			1);

	public static <E> Transformer<E, Integer> zero() {
		@SuppressWarnings("unchecked")
		final Transformer<E, Integer> zero = ZERO;
		return zero;
	}

	public static <E> Transformer<E, Integer> one() {
		@SuppressWarnings("unchecked")
		final Transformer<E, Integer> one = ONE;
		return one;
	}
}
