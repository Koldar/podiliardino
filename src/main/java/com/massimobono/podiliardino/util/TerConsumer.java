package com.massimobono.podiliardino.util;

@FunctionalInterface
public interface TerConsumer<I1, I2, I3> {

	public void consume(I1 input1, I2 input2, I3 input3);
}
