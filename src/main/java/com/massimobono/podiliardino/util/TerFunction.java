package com.massimobono.podiliardino.util;

@FunctionalInterface
public interface TerFunction<I1,I2,I3, O> {

	public O apply(I1 input1, I2 input2, I3 input3);
}
