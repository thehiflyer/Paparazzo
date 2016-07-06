package se.hiflyer.paparazzo.impl;

import se.hiflyer.paparazzo.interfaces.Path;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SimplePath<T> implements Path<T> {

	private List<T> nodes = new LinkedList<T>();

	public SimplePath(T currentNode) {
		nodes.add(currentNode);
	}

	public SimplePath() {
	}

	public void add(T node) {
		nodes.add(node);
	}

	@Override
	public Iterator<T> iterator() {
		return nodes.iterator();
	}

	public int size() {
		return nodes.size();
	}

	@Override
    public String toString() {
        return "SimplePath{" +
                "nodes=" + nodes +
                '}';
    }
}
