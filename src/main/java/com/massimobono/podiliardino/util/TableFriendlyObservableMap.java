package com.massimobono.podiliardino.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.massimobono.podiliardino.dao.DAO;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableView;

/**
 * Represents a {@link ObservableMap} that can be used in tandem with {@link TableView}
 * 
 * Since {@link TableView} supports only {@link ObservableList}, and since {@link DAO} instances usually
 * stores all the needed references, we need a map indexed by id and that can be observed by a {@link TableView}.
 * Hence this class.
 *  
 * @author massi
 *
 * @param <K> the key of the map
 * @param <V> the value of the map
 */
public class TableFriendlyObservableMap<K,V> implements javafx.collections.ObservableMap<K, V> {
	
	private javafx.collections.ObservableMap<K,V> map;
	private ObservableList<K> keyList;
	private ObservableList<V> valueList;
	
	public TableFriendlyObservableMap(Map<K,V> map) {
		this.map = FXCollections.observableMap(map);
		this.keyList = FXCollections.observableArrayList();
		this.valueList = FXCollections.observableArrayList();
		this.keyList.addAll(map.keySet());
		this.valueList.addAll(map.values());
	}
	
	public TableFriendlyObservableMap() {
		this(new HashMap<>());
	}
	
	public ObservableList<K> observableKeyList() {
		return this.keyList;
	}
	
	public ObservableList<V> observableValueList() {
		return this.valueList;
	}
	
	@Override
	public void clear() {
		this.map.clear();
		this.keyList.clear();
		this.valueList.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return this.map.entrySet();
	}

	@Override
	public V get(Object key) {
		return this.map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	@Override
	public V put(K key, V value) {
		this.keyList.add(key);
		this.valueList.add(value);
		return this.map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		this.keyList.addAll(m.keySet());
		this.valueList.addAll(m.values());
		this.map.putAll(m);
	}

	@Override
	public V remove(Object key) {
		this.keyList.remove(key);
		this.valueList.remove(this.map.get(key));
		return this.map.remove(key);
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public Collection<V> values() {
		return this.map.values();
	}

	@Override
	public void addListener(InvalidationListener listener) {
		this.map.addListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		this.map.removeListener(listener);
	}

	@Override
	public void addListener(MapChangeListener<? super K, ? super V> listener) {
		this.map.addListener(listener);
	}

	@Override
	public void removeListener(MapChangeListener<? super K, ? super V> listener) {
		this.map.removeListener(listener);
	}

}
