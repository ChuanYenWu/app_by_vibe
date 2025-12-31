package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.BookRepository
import com.example.myapplication.data.repository.BookRepositoryImpl

interface AppContainer {
    val bookRepository: BookRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val bookRepository: BookRepository by lazy {
        BookRepositoryImpl(
            database.bookDao(),
            database.authorDao(),
            database.genreDao(),
            database.tagDao()
        )
    }
}
