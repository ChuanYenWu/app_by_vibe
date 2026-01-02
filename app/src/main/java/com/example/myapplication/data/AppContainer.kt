package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.BookRepository
import com.example.myapplication.data.repository.BookRepositoryImpl
import com.example.myapplication.data.repository.BackupRepository

interface AppContainer {
    val bookRepository: BookRepository
    val backupRepository: BackupRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val bookRepository: BookRepository by lazy {
        BookRepositoryImpl(
            AppDatabase.getDatabase(context).bookDao(),
            AppDatabase.getDatabase(context).authorDao(),
            AppDatabase.getDatabase(context).genreDao(),
            AppDatabase.getDatabase(context).tagDao()
        )
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepository(AppDatabase.getDatabase(context).backupDao())
    }
}
