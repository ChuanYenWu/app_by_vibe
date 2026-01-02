package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.*

@Dao
interface BackupDao {

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    @Query("SELECT * FROM authors")
    suspend fun getAllAuthors(): List<Author>

    @Query("SELECT * FROM genres")
    suspend fun getAllGenres(): List<Genre>

    @Query("SELECT * FROM tags")
    suspend fun getAllTags(): List<Tag>

    @Query("SELECT * FROM book_links")
    suspend fun getAllBookLinks(): List<BookLink>

    @Query("SELECT * FROM author_links")
    suspend fun getAllAuthorLinks(): List<AuthorLink>

    @Query("SELECT * FROM book_author_cross_ref")
    suspend fun getAllBookAuthorCrossRefs(): List<BookAuthorCrossRef>

    @Query("SELECT * FROM book_genre_cross_ref")
    suspend fun getAllBookGenreCrossRefs(): List<BookGenreCrossRef>

    @Query("SELECT * FROM book_tag_cross_ref")
    suspend fun getAllBookTagCrossRefs(): List<BookTagCrossRef>

    @Transaction
    suspend fun restoreDatabase(
        books: List<Book>,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        bookLinks: List<BookLink>,
        authorLinks: List<AuthorLink>,
        bookAuthorCrossRefs: List<BookAuthorCrossRef>,
        bookGenreCrossRefs: List<BookGenreCrossRef>,
        bookTagCrossRefs: List<BookTagCrossRef>,
        clearExisting: Boolean = true
    ) {
        if (clearExisting) {
            clearAllTables()
        }

        insertBooks(books)
        insertAuthors(authors)
        insertGenres(genres)
        insertTags(tags)
        insertBookLinks(bookLinks)
        insertAuthorLinks(authorLinks)
        insertBookAuthorCrossRefs(bookAuthorCrossRefs)
        insertBookGenreCrossRefs(bookGenreCrossRefs)
        insertBookTagCrossRefs(bookTagCrossRefs)
    }

    @Query("DELETE FROM books")
    suspend fun clearBooks()

    @Query("DELETE FROM authors")
    suspend fun clearAuthors()

    @Query("DELETE FROM genres")
    suspend fun clearGenres()

    @Query("DELETE FROM tags")
    suspend fun clearTags()

    @Transaction
    suspend fun clearAllTables() {
        // Order matters if foreign keys are enforced with RESTRICT, 
        // but design uses CASCADE logic usually. 
        // To be safe, clear junction tables and links first.
        clearBookAuthorCrossRefs()
        clearBookGenreCrossRefs()
        clearBookTagCrossRefs()
        clearBookLinks()
        clearAuthorLinks()
        clearBooks()
        clearAuthors()
        clearGenres()
        clearTags()
    }

    @Query("DELETE FROM book_author_cross_ref")
    suspend fun clearBookAuthorCrossRefs()

    @Query("DELETE FROM book_genre_cross_ref")
    suspend fun clearBookGenreCrossRefs()

    @Query("DELETE FROM book_tag_cross_ref")
    suspend fun clearBookTagCrossRefs()

    @Query("DELETE FROM book_links")
    suspend fun clearBookLinks()

    @Query("DELETE FROM author_links")
    suspend fun clearAuthorLinks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthors(authors: List<Author>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<Genre>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookLinks(links: List<BookLink>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthorLinks(links: List<AuthorLink>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookAuthorCrossRefs(refs: List<BookAuthorCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookGenreCrossRefs(refs: List<BookGenreCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookTagCrossRefs(refs: List<BookTagCrossRef>)
}
