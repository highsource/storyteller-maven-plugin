package org.highsource.storyteller.jung.algorithms.rank;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;

public class ConstantTransformer<E> implements Transformer<E, Integer> {

	private final int value;

	public ConstantTransformer(int value) {
		Validate.isTrue(value >= 0);
		this.value = value;
	}

	@Override
	public Integer transform(E edge) {
		return this.value;
	}

	@SuppressWarnings("rawtypes")
	private static final Transformer ZERO = new ConstantTransformer(0);
	@SuppressWarnings("rawtypes")
	private static final Transformer ONE = new ConstantTransformer(1);

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
