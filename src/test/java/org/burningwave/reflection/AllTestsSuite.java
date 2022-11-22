package org.burningwave.reflection;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
//@SelectPackages("org.burningwave.reflection")
@SelectClasses({
    ConstructorsTest.class,
    FieldsTest.class,
    FieldAccessorTest.class,
    MembersTest.class,
    MethodsTest.class,
    DriverTest.class,
    CacheTest.class,
    RepeatedConstructorTest.class,
    RepeatedFieldsTest.class,
    RepeatedFieldAccessorTest.class,
    RepeatedMembersTest.class,
    RepeatedMethodsTest.class
})
public class AllTestsSuite {

}
