package org.playorm.nio.impl.libs;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 */
public class MyBlockingQueue<T> implements BlockingQueue<Runnable>
{
    private int timeoutMillis = 10000;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private ArrayBlockingQueue<Runnable> queue;

    /**
     * Creates an instance of MyBlockingQueue.
     * @param queue
     */
    public MyBlockingQueue(ArrayBlockingQueue<Runnable> queue)
    {
        this.queue = queue;
    }

    public boolean add(Runnable o)
    {
        try
        {
            boolean isSuccess = queue.offer(o, timeoutMillis, TimeUnit.MILLISECONDS);
            if(!isSuccess)
                throw new IllegalStateException("Queue was full for "+unit.toSeconds(timeoutMillis)+" seconds");
            return isSuccess;
        }
        catch(InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public boolean addAll(Collection< ? extends Runnable> c)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    public void clear()
    {
        queue.clear();
    }

    public boolean contains(Object o)
    {
        return queue.contains(o);
    }

    public boolean containsAll(Collection< ? > c)
    {
        return queue.containsAll(c);
    }

    public int drainTo(Collection< ? super Runnable> c, int maxElements)
    {
        return queue.drainTo(c, maxElements);
    }

    public int drainTo(Collection< ? super Runnable> c)
    {
        return queue.drainTo(c);
    }

    public Runnable element()
    {
        return queue.element();
    }

    public boolean equals(Object obj)
    {
        return queue.equals(obj);
    }

    public int hashCode()
    {
        return queue.hashCode();
    }

    public boolean isEmpty()
    {
        return queue.isEmpty();
    }

    public Iterator<Runnable> iterator()
    {
        return queue.iterator();
    }

    public boolean offer(Runnable o, long timeout, TimeUnit unit) throws InterruptedException
    {
        return queue.offer(o, timeout, unit);
    }

    public boolean offer(Runnable o)
    {
        //need to change this to block for a bit....
        try
        {
            boolean isSuccess = queue.offer(o, timeoutMillis, TimeUnit.MILLISECONDS);
            if(!isSuccess)
                throw new IllegalStateException("Queue was full for "+unit.toSeconds(timeoutMillis)+" seconds");
            return isSuccess;
        }
        catch(InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Runnable peek()
    {
        return queue.peek();
    }

    public Runnable poll()
    {
        return queue.poll();
    }

    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        return queue.poll(timeout, unit);
    }

    public void put(Runnable o) throws InterruptedException
    {
        queue.put(o);
    }

    public int remainingCapacity()
    {
        return queue.remainingCapacity();
    }

    public Runnable remove()
    {
        return queue.remove();
    }

    public boolean remove(Object o)
    {
        return queue.remove(o);
    }

    public boolean removeAll(Collection< ? > c)
    {
        return queue.removeAll(c);
    }

    public boolean retainAll(Collection< ? > c)
    {
        return queue.retainAll(c);
    }

    public int size()
    {
        return queue.size();
    }

    public Runnable take() throws InterruptedException
    {
        return queue.take();
    }

    public Object[] toArray()
    {
        return queue.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return queue.toArray(a);
    }

    public String toString()
    {
        return queue.toString();
    }

    

}
