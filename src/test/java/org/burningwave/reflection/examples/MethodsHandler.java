package org.burningwave.reflection.examples;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Collection;

import org.burningwave.Classes;
import org.burningwave.reflection.MethodCriteria;
import org.burningwave.reflection.Methods;


public class MethodsHandler {

    public static void execute() {
        Methods.INSTANCE.invoke(System.out, "println", "Hello World");
        Integer number = Methods.INSTANCE.invokeStatic(Integer.class, "valueOf", 1);

        //Invoking a method: the invoke method tries to execute the target method via MethodHandle and
        //if that fails it tries with standard reflection
        Methods.INSTANCE.invoke(System.out, "println", number);

        //Filtering and obtaining a MethodHandle reference
        MethodHandle methodHandle = Methods.INSTANCE.findFirstDirectHandle(
            MethodCriteria.byScanUpTo((cls) ->
                //We only analyze the ClassLoader class and not all of its hierarchy (default behavior)
                cls.getName().equals(ClassLoader.class.getName())
            ).name(
                "defineClass"::equals
            ).and().parameterTypes(params ->
                params.length == 3
            ).and().parameterTypesAreAssignableFrom(
                String.class, ByteBuffer.class, ProtectionDomain.class
            ).and().returnType((cls) ->
                cls.getName().equals(Class.class.getName())
            ), ClassLoader.class
        );

        //Filtering and obtaining all methods of ClassLoader class that have at least
        //one input parameter of Class type
        Collection<Method> methods = Methods.INSTANCE.findAll(
            MethodCriteria.byScanUpTo((cls) ->
                //We only analyze the ClassLoader class and not all of its hierarchy (default behavior)
                cls.getName().equals(ClassLoader.class.getName())
            ).parameter((params, idx) -> {
                return Classes.INSTANCE.isAssignableFrom(params[idx].getType(), Class.class);
            }), ClassLoader.class
        );
    }

    public static void main(String[] args) {
        execute();
    }

}
