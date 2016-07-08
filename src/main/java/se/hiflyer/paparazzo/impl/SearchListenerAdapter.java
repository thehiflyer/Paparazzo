package se.hiflyer.paparazzo.impl;

import se.hiflyer.paparazzo.interfaces.SearchListener;

public class SearchListenerAdapter<T> implements SearchListener<T> {
	@Override
	public void addedToOpenSet(T node) {
	}

	@Override
	public void addedToClosedSet(T node) {
	}

	@Override
	public void updatedGCost(T node, double cost) {
	}
}
