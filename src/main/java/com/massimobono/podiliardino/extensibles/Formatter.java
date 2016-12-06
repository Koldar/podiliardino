package com.massimobono.podiliardino.extensibles;

@FunctionalInterface
public interface Formatter<IN, OUT> {

	public OUT format(IN toFormat);
}
