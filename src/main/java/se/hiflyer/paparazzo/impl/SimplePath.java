package se.hiflyer.paparazzo.impl;

import se.hiflyer.paparazzo.interfaces.Path;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SimplePath<T> implements Path<T> {

	List<T> nodes = new LinkedList<T>();

	public SimplePath(T currentNode) {
		nodes.add(currentNode);
	}

	public SimplePath() {
	}

	@Override
	public void add(T node) {
		nodes.add(node);
	}

	@Override
	public Iterator<T> iterator() {
		return nodes.iterator();
	}
}
