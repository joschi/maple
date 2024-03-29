package maple.core.internals;


/**
 * The thread creator is a utility class that allows us to create worker threads in maple.
 * They have a few properties in common such as being daemon threads and belonging to the same {@link ThreadGroup}
 * <br />
 * @see Thread#setDaemon(boolean)
 */
public final class ThreadCreator {
    private ThreadCreator() {}
    private static final ThreadGroup mapleThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "maple");

    public static Thread newThread(String name, Runnable target){
        var thread = new Thread(mapleThreadGroup, target);

        // We want to be able to identify the thread
        thread.setName(name);

        // Thread has to be a daemon thread in order for us to be able to close the JVM while it still runs.
        thread.setDaemon(true);
        return thread;
    }
}
