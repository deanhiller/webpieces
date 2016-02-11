package org.playorm.nio.impl.libs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.libs.ChannelsRunnable;


/**
 */
public class LookAheadQueue implements BlockingQueue<Runnable>
{
    //The list of sessions currently being run on a thread(ie. don't let another thread run a request of one of these sessions)
    private Set<RegisterableChannel> runningChannels = Collections.synchronizedSet(new HashSet<RegisterableChannel>());
    private BlockingQueue<Runnable> queue;
    private Lock lock = new ReentrantLock();
    private Condition hasAvailableRunnable = lock.newCondition();
    private Condition queueNotFull = lock.newCondition();
    
    public LookAheadQueue(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }
    
    public boolean offer(Runnable o)
    {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public boolean offer(Runnable o, long timeout, TimeUnit unit) throws InterruptedException
    {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void put(Runnable o) throws InterruptedException
    {
        lock.lockInterruptibly();
        try {
            while(queue.remainingCapacity() <= 0) {
                queueNotFull.await();
            }
            queue.put(o);
            
            //now may need to signal hasAvailableRunnable
            ChannelsRunnable r = (ChannelsRunnable)o;
            if(!runningChannels.contains(r.getChannel()))
                hasAvailableRunnable.signal();
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see java.util.concurrent.BlockingQueue#remainingCapacity()
     */
    public int remainingCapacity()
    {
        return queue.remainingCapacity();
    }

    public boolean add(Runnable o)
    {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
     */
    public int drainTo(Collection< ? super Runnable> c)
    {
        throw new UnsupportedOperationException("not supported yet");
    }

    /**
     * @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
     */
    public int drainTo(Collection< ? super Runnable> c, int maxElements)
    {
        throw new UnsupportedOperationException("not supported yet");
    }

    /**
     * @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
     */
    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        lock.lockInterruptibly();
        try {
            long nanos = unit.toNanos(timeout);
            while(true) {                
                if(queue.remainingCapacity() > 0) {
                    ChannelsRunnable availableRunnable = getAvailableRunnable();
                    if(availableRunnable != null)
                        return availableRunnable;
                }
                
                if (nanos <= 0)
                    return null;
                try {
                    nanos = hasAvailableRunnable.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    hasAvailableRunnable.signal(); // propagate to non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            lock.unlock();
        }        
    }
    
    public Runnable take() throws InterruptedException
    {
        lock.lockInterruptibly();
        try {
            try {
                ChannelsRunnable r = null;
                while(r == null) {
                    r = getAvailableRunnable();
                    if(r != null)
                        break;
                    hasAvailableRunnable.await();
                }
                return r;
            } catch (InterruptedException ie) {
                hasAvailableRunnable.signal(); // propagate to non-interrupted thread
                throw ie;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Returns an available runnable.  A runnable is only available if another thread
     * is not running another runnable that has the same channel(ie. it is the same client
     * making the request.).  this makes it very fair between clients so they all get time in
     * the threads equally
     * @return Available runnable or null if none is available
     */
    private ChannelsRunnable getAvailableRunnable()
    {
        Iterator<Runnable> iterator = queue.iterator();
        while(iterator.hasNext()) {
            ChannelsRunnable runnable = (ChannelsRunnable)iterator.next();
            RegisterableChannel channel = runnable.getChannel();
            if(!runningChannels.contains(channel)) {
                runningChannels.add(channel);                
                return new QueueRunnable(runningChannels, runnable);
            }
        }
        return null;
    }
    
    private class QueueRunnable implements ChannelsRunnable {
        private Set<RegisterableChannel> runningChannels;
        private ChannelsRunnable runnable;

        public QueueRunnable(Set<RegisterableChannel> runningChannels, ChannelsRunnable runnable) {
            this.runningChannels = runningChannels;
            this.runnable = runnable;
        }

        public void run() {
            runnable.run();
            
            lock.lock();
            try {
                runningChannels.remove(getChannel());
                hasAvailableRunnable.signal();
            } finally {
                lock.unlock();
            }
        }

        public RegisterableChannel getChannel() {
            return runnable.getChannel();
        }
    }
    
    public Runnable poll()
    {
        return getAvailableRunnable();
    }

    /**
     * @see java.util.Queue#remove()
     */
    public Runnable remove()
    {
        throw new UnsupportedOperationException("not supported yet");
    }

    /**
     * @see java.util.Queue#peek()
     */
    public Runnable peek()
    {
        return queue.peek();
    }

    /**
     * @see java.util.Queue#element()
     */
    public Runnable element()
    {
        return queue.element();
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size()
    {
        return queue.size();
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty()
    {
        return queue.isEmpty();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o)
    {
        return queue.contains(o);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator<Runnable> iterator()
    {
        return queue.iterator();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray()
    {
        return queue.toArray();
    }

    /**
     * @see java.util.Collection#toArray(T[])
     */
    public <T> T[] toArray(T[] a)
    {
        return queue.toArray(a);
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o)
    {
        return queue.remove(o);
    }

    /**
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection< ? > c)
    {
        return queue.containsAll(c);
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection< ? extends Runnable> c)
    {
        return queue.addAll(c);
    }

    /**
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection< ? > c)
    {
        return queue.removeAll(c);
    }

    /**
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection< ? > c)
    {
        return queue.retainAll(c);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear()
    {
        queue.clear();
    }


}
