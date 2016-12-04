package com.massimobono.podiliardino.extensibles.ranking;

@FunctionalInterface
public interface Formatter<IN, OUT> {

	public OUT format(IN toFormat);
}
