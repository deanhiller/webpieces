package org.webpieces.util.cmdline2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FetchValue {

    public <T> Supplier<T> fetchFinalValue(T testDefault, Supplier<SupplierImpl<T>> supplier, Supplier<SupplierImpl<T>> defSupplier) {
        return supplier.get();
    }
}
