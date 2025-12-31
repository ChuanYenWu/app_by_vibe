package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.*
import kotlinx.coroutines.flow.Flow

import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface BookDao {
    @Transaction
    @RawQuery(observedEntities = [Book::class, BookAuthorCrossRef::class, BookGenreCrossRef::class, BookTagCrossRef::class, BookLink::class, Author::class, Genre::class, Tag::class])
    fun getBooksByFilter(query: SupportSQLiteQuery): Flow<List<BookWithInfo>>

    @Transaction
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<BookWithInfo>>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): Flow<BookWithInfo?>
    
    @Query("SELECT * FROM books WHERE title = :title LIMIT 1")
    suspend fun getBookByTitle(title: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookAuthorCrossRef(crossRef: BookAuthorCrossRef)
    
    @Delete
    suspend fun deleteBookAuthorCrossRef(crossRef: BookAuthorCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookGenreCrossRef(crossRef: BookGenreCrossRef)
    
    @Delete
    suspend fun deleteBookGenreCrossRef(crossRef: BookGenreCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookTagCrossRef(crossRef: BookTagCrossRef)
    
    @Delete
    suspend fun deleteBookTagCrossRef(crossRef: BookTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookLink(link: BookLink)

    @Delete
    suspend fun deleteBookLink(link: BookLink)
    
    @Query("DELETE FROM book_links WHERE bookId = :bookId")
    suspend fun deleteLinksByBookId(bookId: Long)
    
    @Query("DELETE FROM book_author_cross_ref WHERE bookId = :bookId")
    suspend fun deleteAuthorCrossRefsByBookId(bookId: Long)
    
    @Query("DELETE FROM book_genre_cross_ref WHERE bookId = :bookId")
    suspend fun deleteGenreCrossRefsByBookId(bookId: Long)
    
    @Query("DELETE FROM book_tag_cross_ref WHERE bookId = :bookId")
    suspend fun deleteTagCrossRefsByBookId(bookId: Long)
}
