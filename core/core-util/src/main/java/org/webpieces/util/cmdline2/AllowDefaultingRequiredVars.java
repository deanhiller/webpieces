package org.webpieces.util.cmdline2;

import java.util.function.Supplier;

public class AllowDefaultingRequiredVars extends FetchValue {

    @Override
    public <T> Supplier<T> fetchFinalValue(T testDefault, Supplier<SupplierImpl<T>> supplier, Supplier<SupplierImpl<T>> defSupplier) {
        if(testDefault != null) {
            return defSupplier.get();
        }
        return supplier.get();
    }
}
