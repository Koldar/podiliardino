package com.massimobono.podiliardino.model;

import java.util.NoSuchElementException;

import com.massimobono.podiliardino.util.I18N;

public enum MatchStatus {
	TODO(0, "todo"),
	DONE(1, "done");
	
	private int id;
	private String toString;
	
	private MatchStatus(int dbId, String toString) {
		this.id = dbId;
		this.toString = toString;
	}
	
	public static MatchStatus from(int i) throws NoSuchElementException {
		for (MatchStatus ms : MatchStatus.values()) {
			if (ms.id == i) {
				return ms;
			}
		}
		throw new NoSuchElementException(String.format("can't find status with id %d", i));
	}
	
	public String toString() {
		return I18N.get().getString(this.toString);
	}
	
	public int getId() {
		return this.id;
	}
	
	
}
