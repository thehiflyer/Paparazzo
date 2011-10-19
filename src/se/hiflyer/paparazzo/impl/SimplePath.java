package se.hiflyer.paparazzo.impl;

import com.google.common.collect.Lists;
import se.hiflyer.paparazzo.interfaces.Path;

import java.util.Iterator;
import java.util.List;

public class SimplePath<T> implements Path<T> {

	List<T> nodes = Lists.newLinkedList();

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
