package se.hiflyer.paparazzo.interfaces;

public interface Path<T> extends Iterable<T> {
	void add(T node);
}
