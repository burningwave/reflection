package org.burningwave.reflection;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@SuppressWarnings("unused")
@RunWith(JUnitPlatform.class)
//@SelectPackages("org.burningwave.core")
@SelectClasses({
    ConstructorsTest.class,
    FieldsTest.class,
    FieldAccessorTest.class,
    MembersTest.class,
    MethodsTest.class,
})
@ExcludeTags("Heavy")
public class AllExceptHeavyTestsSuite {

}