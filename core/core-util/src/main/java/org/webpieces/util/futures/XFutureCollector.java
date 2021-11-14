package org.webpieces.util.futures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class XFutureCollector<X, T extends XFuture<X>> implements Collector<T, List<T>, XFuture<List<X>>>  {

    private XFutureCollector(){
    }

    public static <X, T extends XFuture<X>> Collector<T, List<T>, XFuture<List<X>>> allOf(){
        return new XFutureCollector<>();
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (left, right) -> { left.addAll(right); return left; };
    }

    @Override
    public Function<List<T>, XFuture<List<X>>> finisher() {
        return ls->XFuture.allOf(ls.toArray(new XFuture[ls.size()]))
                .thenApply(v -> ls
                        .stream()
                        .map(XFuture::join)
                        .collect(Collectors.toList()));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
