package se.hiflyer.paparazzo.interfaces;

public interface NeighbourLookup<T> {
	Iterable<T> getNeighbours(T x);
}
