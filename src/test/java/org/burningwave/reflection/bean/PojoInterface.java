package org.burningwave.reflection.bean;

import java.util.List;

public interface PojoInterface {

	public <T> List<T> getList();

	public <T> void setList(List<T> value);

	public boolean isValid();

}