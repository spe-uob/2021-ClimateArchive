package org.climatearchive.climatearchive.util;

public class Pair<V, T> {
    private final V first;
    private final T second;

    public Pair(V first, T second) {
        this.first = first;
        this.second = second;
    }

    public V getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
