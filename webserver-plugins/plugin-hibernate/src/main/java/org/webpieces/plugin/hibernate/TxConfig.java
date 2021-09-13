package org.webpieces.plugin.hibernate;

public class TxConfig {
    private boolean transactionOnByDefault;

    public TxConfig(boolean transactionOnByDefault) {
        this.transactionOnByDefault = transactionOnByDefault;
    }

    public boolean isTransactionOnByDefault() {
        return transactionOnByDefault;
    }
}
