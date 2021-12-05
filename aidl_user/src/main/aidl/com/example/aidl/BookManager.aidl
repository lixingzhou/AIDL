// BookManager.aidl
package com.example.aidl;

// Declare any non-default types here with import statements
import com.example.aidl.Book;
import com.example.aidl.IOnNewBookArrivedListener;

interface BookManager {

    List getBooks();
    void addBook(in Book book);

    //添加监听相关  原始版本
    void  registerListener(in IOnNewBookArrivedListener listener);
    void unRegisterListener(in IOnNewBookArrivedListener listener);
}