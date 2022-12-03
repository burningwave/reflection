package org.burningwave.reflection.examples;

import org.burningwave.reflection.FieldAccessor;
import org.burningwave.reflection.examples.bean.Complex;

public class GetAndSetPropertiesThroughPath{

    public void execute() {
        Complex complex = new Complex();
        //The field accessor try to access by field introspection: if no field was found
        //it will search getter method and invokes it
        String nameFromObjectInArray = FieldAccessor.INSTANCE.get(complex, "data.items[1][0].name");
        String nameFromObjectMap = FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name");
        System.out.println(nameFromObjectInArray);
        System.out.println(nameFromObjectMap);
        nameFromObjectInArray = FieldAccessor.INSTANCE.get(complex, "data.items[1][2].name");
        nameFromObjectMap = FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name");
        System.out.println(nameFromObjectInArray);
        System.out.println(nameFromObjectMap);
        FieldAccessor.INSTANCE.set(complex, "data.itemsMap[items][1][1].name", "Good evening!");
        nameFromObjectInArray = FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name");
        System.out.println(nameFromObjectInArray);
    }

    public static void main(String[] args) {
        new GetAndSetPropertiesThroughPath().execute();
    }

}