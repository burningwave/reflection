package org.burningwave.reflection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConstructorsTest.class,
    FieldsTest.class,
    FieldAccessorTest.class,
    MembersTest.class,
    MethodsTest.class
})
public class AllTestsSuite {

}
