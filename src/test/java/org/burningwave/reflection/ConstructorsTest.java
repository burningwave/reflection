package org.burningwave.reflection;


import org.burningwave.reflection.Constructors;
import org.burningwave.reflection.service.ExtendedService;
import org.junit.jupiter.api.Test;

@SuppressWarnings("all")
public class ConstructorsTest extends BaseTest {

	@Test
	public void newInstanceDirectOfTestOne() {
		testNotNull(() -> Constructors.INSTANCE.newInstanceDirectOf(ExtendedService.class));
	}

	@Test
	public void newInstanceDirectOfTestTwo() {
		testNotNull(() -> Constructors.INSTANCE.newInstanceDirectOf(
				ExtendedService.class,
				"Extended service name"
			)
		);
	}

	@Test
	public void newInstanceOfTestOne() {
		testNotNull(() -> Constructors.INSTANCE.newInstanceOf(ExtendedService.class));
	}

	@Test
	public void newInstanceOfTestTwo() {
		testNotNull(() -> Constructors.INSTANCE.newInstanceOf(
				ExtendedService.class,
				"Extended service name"
			)
		);
	}

	@Test
	public void convertToMethodHandleTestOne() {
		testNotNull(() ->
			Constructors.INSTANCE.findDirectHandle(
				Constructors.INSTANCE.findOneAndMakeItAccessible(ExtendedService.class)
			).invokeWithArguments()
		);
	}

}
