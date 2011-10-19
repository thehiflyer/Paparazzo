package se.hiflyer.paparazzo.interfaces;

public interface HeuristicEstimator<T> {
	double estimate(T start, T goal);
}
