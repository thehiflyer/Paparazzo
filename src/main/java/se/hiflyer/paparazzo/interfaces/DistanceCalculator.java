package se.hiflyer.paparazzo.interfaces;

public interface DistanceCalculator<T> {
	double getDistanceBetween(T x, T y);
}
