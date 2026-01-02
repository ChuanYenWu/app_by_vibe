package com.example.myapplication.data.repository

import com.example.myapplication.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    val allBooks: Flow<List<BookWithInfo>>
    val allAuthors: Flow<List<Author>>
    val allGenres: Flow<List<Genre>>
    val allTags: Flow<List<Tag>>

    fun getBooksByFilter(
        sortOrder: SortOrder,
        filterReadingStatus: String? = null,
        filterAuthorIds: List<Long> = emptyList(),
        filterGenreIds: List<Long> = emptyList(),
        filterTagIds: List<Long> = emptyList()
    ): Flow<List<BookWithInfo>>

    fun getBookById(id: Long): Flow<BookWithInfo?>
    
    suspend fun insertBook(
        book: Book,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        links: List<BookLink>
    )
    
    suspend fun updateBook(
        book: Book,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        links: List<BookLink>
    )
    
    suspend fun deleteBook(book: Book)

    // Helper methods for fetching existing entities (for duplicate checking, etc.)
    suspend fun getBookByTitle(title: String): Book?
    suspend fun getAuthorByName(name: String): Author?

    // Entity Management
    suspend fun updateAuthor(author: Author)
    suspend fun deleteAuthor(author: Author)
    suspend fun updateGenre(genre: Genre)
    suspend fun deleteGenre(genre: Genre)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}
