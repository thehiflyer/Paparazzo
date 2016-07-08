package se.hiflyer.paparazzo.interfaces;

public interface SearchListener<T> {
	void addedToOpenSet(T node);

	void addedToClosedSet(T node);

	void updatedGCost(T node, double cost);
}
