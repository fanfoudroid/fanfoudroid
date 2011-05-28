package com.temp.afan;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.content.Context;
import android.os.Process;

public class Controller implements Runnable {
    private static final String TAG = "Contoller";

    private final Context mContext;

    private static Controller sInstance;
    private final BlockingQueue<Command> mCommands = new LinkedBlockingDeque<Command>();
    private final GroupMessagingListener mListeners = new GroupMessagingListener();
    private final Thread mThread;
    private boolean mBusy;

    protected Controller(Context context) {
        mContext = context;

        mThread = new Thread(this);
        mThread.start();
    }

    public synchronized static Controller getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new Controller(context);
        }
        return sInstance;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            Command command;
            try {
                command = mCommands.take();
            } catch (InterruptedException e) {
                continue;
            }
            if (command.listener == null || isActiveListener(command.listener)) {
                mBusy = true;
                command.runnable.run();
                mListeners.controllerCommandCompleted(mCommands.size() > 0);
            }
            mBusy = false;
        }

    }

    private void put(String description, MessagingListener listener,
            Runnable runnable) {
        try {
            Command command = new Command();
            command.listener = listener;
            command.runnable = runnable;
            command.description = description;
            mCommands.add(command);
        } catch (IllegalStateException ie) {
            throw new Error(ie);
        }
    }

    public void addListener(MessagingListener listener) {
        mListeners.addListener(listener);
    }

    public void removeListener(MessagingListener listener) {
        mListeners.removeListener(listener);
    }

    private boolean isActiveListener(MessagingListener listener) {
        return mListeners.isActiveListener(listener);
    }

    /**
     *
     */
    public void loadStatusesForView(final int statusType,
            MessagingListener listener) {
        // mListeners.
        put("listStatusForView", listener, new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // fetch data from database or server

                // Notify UI
                // mListeners.
            }
        });
    }

    /**
     * 
     */
    public void syncStatuses(final int statusType, MessagingListener listener) {
        // mListeners.
        put("syncStatuses", listener, new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // fetch new statuses from server and store them into DB
                
                // Notify UI or ...
            }
        });
    }
    
    public interface Result {
        
    }

    /**
     * 
     */
    private static class Command {
        public Runnable runnable;

        public MessagingListener listener;

        public String description;

        @Override
        public String toString() {
            return description;
        }
    }

}
