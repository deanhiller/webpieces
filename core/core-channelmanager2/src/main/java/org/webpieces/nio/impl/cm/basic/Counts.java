package org.webpieces.nio.impl.cm.basic;

public class Counts {
    private int reads;
    private int writes;
    private int accepts;
    private int connects;

    public void addRead() {
        reads++;
    }
    public void addWrite() {
        writes++;
    }
    public void addAccept() {
        accepts++;
    }
    public void addConnect() {
        connects++;
    }

    public int getReads() {
        return reads;
    }

    public int getWrites() {
        return writes;
    }

    public int getAccepts() {
        return accepts;
    }

    public int getConnects() {
        return connects;
    }

    @Override
    public String toString() {
        return "Counts{" +
                "reads=" + reads +
                ", writes=" + writes +
                ", accepts=" + accepts +
                ", connects=" + connects +
                '}';
    }
}
