package org.burningwave.reflection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.burningwave.reflection.Members;
import org.burningwave.reflection.MethodCriteria;
import org.burningwave.reflection.service.ExtendedService;
import org.burningwave.reflection.service.Service;
import org.junit.jupiter.api.Test;


public class MembersTest extends BaseTest {

	@Test
	public void findOneTestOne() {
		testNotNull(() ->
			Members.INSTANCE.findOne(
				MethodCriteria.forEntireClassHierarchy().name((name) ->
					name.matches("apply")
				).and().parameterType((params, idx) ->
					idx == 0 && params[idx].equals(Object.class)
				).and().parameterType((params, idx) ->
					idx == 1 && params[idx].equals(String.class)
				).and().parameterType((params, idx) ->
					idx == 2 && params[idx].equals(String.class)
				),
				Service.class
			)
		);
	}


	@Test
	public void findOneTestTwo() {
		testNotNull(() ->
			Members.INSTANCE.findOne(
				MethodCriteria.forEntireClassHierarchy()
				.name((name) -> name.matches("apply"))
				.and().parameterType((params, idx) ->
					idx == 0 && params[idx].equals(Object.class)
				)
				.and().parameterType((params, idx) ->
					idx == 1 && params[idx].equals(String.class)
				)
				.and().parameterType((params, idx) ->
					idx == 2 && params[idx].equals(String.class)
				).skip((initialClass, cls) ->
					Object.class == cls || Service.class == cls
				),
				ExtendedService.class
			)
		);
	}

	@Test
	public void matchOneTestOne() {
		assertTrue(
			Members.INSTANCE.match(
				MethodCriteria.forEntireClassHierarchy()
				.name((name) -> name.matches("apply"))
				.and().parameterType((params, idx) ->
					idx == 0 && params[idx].equals(Object.class)
				)
				.and().parameterType((params, idx) ->
					idx == 1 && params[idx].equals(String.class)
				)
				.and().parameterType((params, idx) ->
					idx == 2 && params[idx].equals(String.class)
				).skip((initialClass, cls) ->
					Object.class == cls || Service.class == cls
				),
				ExtendedService.class
			)
		);
	}

	@Test
	public void findAllTestOne() {
		testNotEmpty(() ->
			Members.INSTANCE.findAll(
				MethodCriteria.forEntireClassHierarchy().name((name) ->
					name.matches("loadClass")
				),
				this.getClass().getClassLoader().getClass()
			)
		);
	}

	@Test
	public void findFirstTestOne() {
		testNotNull(() ->
			Members.INSTANCE.findFirst(
				MethodCriteria.forEntireClassHierarchy().name((name) ->
					name.matches("loadClass")
				),
				this.getClass().getClassLoader().getClass()
			)
		);
	}
}
