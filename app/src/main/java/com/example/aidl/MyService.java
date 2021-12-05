package com.example.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;

public class MyService extends Service {

    private static final String TAG = "Server_Aidl";
    private static InnerHandler mHandler;
    private List<Book> mBooks = new ArrayList<>();

    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListeners = new CopyOnWriteArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        Book book = new Book();
        book.setName("Android开发艺术探索");
        book.setPrice(28);
        mBooks.add(book);
        mHandler = new InnerHandler(this);
        mHandler.sendEmptyMessage(InnerHandler.MSG_1);
        mHandler.sendEmptyMessageDelayed(InnerHandler.MSG_2, 10 * 1000);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mBookManager;
    }

    private static class InnerHandler extends Handler {

        private static final int MSG_1 = 0x11;
        private static final int MSG_2 = 0x12;

        private final WeakReference<MyService> weakReference;

        public InnerHandler(MyService service) {
            weakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MyService service = weakReference.get();
            if (service != null) {
                switch (msg.what) {
                    case MSG_1:
                        Log.v(TAG, "Books :  = " + service.mBooks.toString());
                        sendEmptyMessageDelayed(MSG_1, 20 * 1000);
                        break;

                    case MSG_2:
                        Book book = new Book("监听测试书籍", 200);
                        try {
                            service.onNewBookArrived(book);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        removeMessages(MSG_2);
                        sendEmptyMessageDelayed(MSG_2, 5 * 1000);
                        break;
                }
            }
        }
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        if (!mBooks.contains(book)) {
            mBooks.add(book);
        }
        Log.d(TAG, "onAddNewBook  --> " + (book == null ? null : book.toString()));
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onNewBookArrived(book);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(InnerHandler.MSG_1);
    }

    private final BookManager.Stub mBookManager = new BookManager.Stub() {
        @Override
        public List getBooks() throws RemoteException {
            synchronized (this) {
                Log.e(TAG, "invoking getBooks() method , now the list is : " + mBooks.toString());
                if (mBooks != null) {
                    return mBooks;
                }
                return new ArrayList<>();
            }
        }

        @Override
        public void addBook(Book book) throws RemoteException {

            synchronized (this) {
                if (mBooks == null) {
                    mBooks = new ArrayList<>();
                }
                if (book == null) {
                    Log.e(TAG, "Book is null in In");
                    book = new Book();
                }
                //尝试修改book的参数，主要是为了观察其到客户端的反馈
                book.setPrice(2333);
                if (!mBooks.contains(book)) {
                    mBooks.add(book);
                }
                //打印mBooks列表，观察客户端传过来的值
                Log.e(TAG, "invoking addBooks() method , now the list is : " + mBooks.toString());
            }
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
            Log.w(TAG, "registerListener --  " + mListeners.size());
        }

        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
            }
            Log.w(TAG, "unRegisterListener -- " + mListeners.size());
        }
    };

}