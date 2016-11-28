package com.massimobono.podiliardino.model;

/**
 * Classes implementing this class have an <tt>id</tt> field unique foreach different instances
 * 
 * Classes with an <tt>id</tt> are indexable because each semantically different instances have 2 different id.
 * An example of indexable classes are those computed from SQL database.
 * 
 * @author massi
 *
 */
public interface Indexable {

	public void setId(long id);
	
	public long getId();
}
