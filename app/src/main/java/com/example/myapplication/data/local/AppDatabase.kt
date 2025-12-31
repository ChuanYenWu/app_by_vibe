package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.*
import com.example.myapplication.data.local.entity.*

@Database(
    entities = [
        Book::class, Author::class, Genre::class, Tag::class, 
        BookLink::class, AuthorLink::class,
        BookAuthorCrossRef::class, BookGenreCrossRef::class, BookTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun genreDao(): GenreDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_app_database"
                )
                .fallbackToDestructiveMigration() // For development only
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
