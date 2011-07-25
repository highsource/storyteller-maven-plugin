package org.highsource.storyteller.jung.algorithms.rank;

import org.apache.commons.lang.Validate;

public class ConstantMinimumLengthConstraint<E> implements
		MinimumLengthConstraint<E> {

	private final int minimumLength;

	public ConstantMinimumLengthConstraint(int minimumLength) {
		Validate.isTrue(minimumLength >= 0);
		this.minimumLength = minimumLength;
	}

	@Override
	public int getMinimumLength(E edge) {
		return this.minimumLength;
	}

	@SuppressWarnings("rawtypes")
	private static final MinimumLengthConstraint ZERO = new ConstantMinimumLengthConstraint(
			0);
	@SuppressWarnings("rawtypes")
	private static final MinimumLengthConstraint ONE = new ConstantMinimumLengthConstraint(
			1);

	public static <E> MinimumLengthConstraint<E> zero() {
		@SuppressWarnings("unchecked")
		final MinimumLengthConstraint<E> zero = ZERO;
		return zero;
	}

	public static <E> MinimumLengthConstraint<E> one() {
		@SuppressWarnings("unchecked")
		final MinimumLengthConstraint<E> one = ONE;
		return one;
	}
}
