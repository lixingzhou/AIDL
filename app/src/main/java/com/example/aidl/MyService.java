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

import androidx.annotation.NonNull;

public class MyService extends Service {

    private static InnerHandler mHandler;
    private List<Book> mBooks = new ArrayList<>();

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Book book = new Book();
        book.setName("Android开发艺术探索");
        book.setPrice(28);
        mBooks.add(book);
        mHandler = new InnerHandler(this);
        mHandler.sendEmptyMessage(InnerHandler.MSG_1);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mBookManager;
    }

    private static class InnerHandler extends Handler {

        private static final int MSG_1 = 0x11;

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
                        Log.d("testInfo", "Books :  = " + service.mBooks.toString());
                        sendEmptyMessageDelayed(MSG_1, 2000);
                        break;
                }
            }
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
                Log.e("testInfo", "invoking getBooks() method , now the list is : " + mBooks.toString());
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
                    Log.e("testInfo", "Book is null in In");
                    book = new Book();
                }
                //尝试修改book的参数，主要是为了观察其到客户端的反馈
                book.setPrice(2333);
                if (!mBooks.contains(book)) {
                    mBooks.add(book);
                }
                //打印mBooks列表，观察客户端传过来的值
                Log.e("testInfo", "invoking addBooks() method , now the list is : " + mBooks.toString());
            }
        }
    };

}