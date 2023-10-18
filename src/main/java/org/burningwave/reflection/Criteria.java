package org.burningwave.reflection;

public class Criteria<E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> extends org.burningwave.Criteria<E, C, T> {

	@Override
	protected C newInstance() {
		return (C)Constructors.INSTANCE.newInstanceOf(this.getClass());
	}

	public static class Simple<E, C extends Simple<E, C>> extends org.burningwave.Criteria.Simple<E, C>{

		@Override
		protected C newInstance() {
			return (C)Constructors.INSTANCE.newInstanceOf(this.getClass());
		}

	}

}
