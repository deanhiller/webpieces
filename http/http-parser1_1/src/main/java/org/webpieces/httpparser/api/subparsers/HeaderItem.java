package org.webpieces.httpparser.api.subparsers;

public class HeaderItem<T> implements Comparable<HeaderItem<T>> {
	
	private double priority;
	private T item;

	public HeaderItem(double priority, T item) {
		this.priority = priority;
		this.item = item;
	}

	public double getPriority() {
		return priority;
	}


	public T getItem() {
		return item;
	}

	@Override
	public int compareTo(HeaderItem<T> o) {
		Double left = new Double(this.priority);
		Double right = new Double(o.priority);
		return right.compareTo(left);
	}
}
