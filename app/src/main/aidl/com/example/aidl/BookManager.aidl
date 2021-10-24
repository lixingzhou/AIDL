// BookManager.aidl
package com.example.aidl;

// Declare any non-default types here with import statements
import com.example.aidl.Book;

interface BookManager {

    List getBooks();
    void addBook(in Book book);

}