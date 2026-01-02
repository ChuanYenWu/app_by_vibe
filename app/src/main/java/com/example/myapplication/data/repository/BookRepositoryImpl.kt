package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.*
import com.example.myapplication.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import androidx.sqlite.db.SimpleSQLiteQuery

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val authorDao: AuthorDao,
    private val genreDao: GenreDao,
    private val tagDao: TagDao
) : BookRepository {

    override val allBooks: Flow<List<BookWithInfo>> = bookDao.getAllBooks()
    override val allAuthors: Flow<List<Author>> = authorDao.getAllAuthors()
    override val allGenres: Flow<List<Genre>> = genreDao.getAllGenres()
    override val allTags: Flow<List<Tag>> = tagDao.getAllTags()

    override fun getBooksByFilter(
        sortOrder: SortOrder,
        filterReadingStatus: String?,
        filterAuthorIds: List<Long>,
        filterGenreIds: List<Long>,
        filterTagIds: List<Long>
    ): Flow<List<BookWithInfo>> {
        val queryBuilder = StringBuilder("SELECT DISTINCT b.* FROM books b")
        val args = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        // Joins for filtering
        if (filterAuthorIds.isNotEmpty()) {
            queryBuilder.append(" INNER JOIN book_author_cross_ref bacr ON b.id = bacr.bookId")
            conditions.add("bacr.authorId IN (${filterAuthorIds.joinToString(",")})")
        }
        if (filterGenreIds.isNotEmpty()) {
            queryBuilder.append(" INNER JOIN book_genre_cross_ref bgcr ON b.id = bgcr.bookId")
            conditions.add("bgcr.genreId IN (${filterGenreIds.joinToString(",")})")
        }
        if (filterTagIds.isNotEmpty()) {
            queryBuilder.append(" INNER JOIN book_tag_cross_ref btcr ON b.id = btcr.bookId")
            conditions.add("btcr.tagId IN (${filterTagIds.joinToString(",")})")
        }

        // Filter by Reading Status
        if (!filterReadingStatus.isNullOrEmpty() && filterReadingStatus != "All") {
            conditions.add("b.readingStatus = ?")
            args.add(filterReadingStatus)
        }

        if (conditions.isNotEmpty()) {
            queryBuilder.append(" WHERE ${conditions.joinToString(" AND ")}")
        }

        // Sorting
        queryBuilder.append(" ORDER BY ")
        when (sortOrder) {
            SortOrder.TITLE_ASC -> queryBuilder.append("b.title ASC")
            SortOrder.TITLE_DESC -> queryBuilder.append("b.title DESC")
            SortOrder.RATING_ASC -> queryBuilder.append("b.rating ASC")
            SortOrder.RATING_DESC -> queryBuilder.append("b.rating DESC")
            SortOrder.DATE_ADDED_ASC -> queryBuilder.append("b.createdAt ASC")
            SortOrder.DATE_ADDED_DESC -> queryBuilder.append("b.createdAt DESC")
            SortOrder.DATE_MODIFIED_ASC -> queryBuilder.append("b.updatedAt ASC")
            SortOrder.DATE_MODIFIED_DESC -> queryBuilder.append("b.updatedAt DESC")
        }

        val query = SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())
        return bookDao.getBooksByFilter(query)
    }

    override fun getBookById(id: Long): Flow<BookWithInfo?> = bookDao.getBookById(id)

    override suspend fun insertBook(
        book: Book,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        links: List<BookLink>
    ) {
        // 1. Insert Book
        val bookId = bookDao.insertBook(book)

        // 2. Handle Authors
        authors.forEach { author ->
            val authorId = getOrCreateAuthorId(author)
            bookDao.insertBookAuthorCrossRef(BookAuthorCrossRef(bookId, authorId))
        }

        // 3. Handle Genres
        genres.forEach { genre ->
            val genreId = getOrCreateGenreId(genre)
            bookDao.insertBookGenreCrossRef(BookGenreCrossRef(bookId, genreId))
        }

        // 4. Handle Tags
        tags.forEach { tag ->
            val tagId = getOrCreateTagId(tag)
            bookDao.insertBookTagCrossRef(BookTagCrossRef(bookId, tagId))
        }

        // 5. Handle Links
        links.forEach { link ->
            bookDao.insertBookLink(link.copy(bookId = bookId))
        }
    }

    override suspend fun updateBook(
        book: Book,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        links: List<BookLink>
    ) {
        val bookId = book.id
        bookDao.updateBook(book)

        // Clear existing relations
        bookDao.deleteAuthorCrossRefsByBookId(bookId)
        bookDao.deleteGenreCrossRefsByBookId(bookId)
        bookDao.deleteTagCrossRefsByBookId(bookId)
        bookDao.deleteLinksByBookId(bookId)

        // Re-insert relations
        authors.forEach { author ->
            val authorId = getOrCreateAuthorId(author)
            bookDao.insertBookAuthorCrossRef(BookAuthorCrossRef(bookId, authorId))
        }
        genres.forEach { genre ->
            val genreId = getOrCreateGenreId(genre)
            bookDao.insertBookGenreCrossRef(BookGenreCrossRef(bookId, genreId))
        }
        tags.forEach { tag ->
            val tagId = getOrCreateTagId(tag)
            bookDao.insertBookTagCrossRef(BookTagCrossRef(bookId, tagId))
        }
        links.forEach { link ->
            bookDao.insertBookLink(link.copy(bookId = bookId))
        }
    }

    override suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    override suspend fun getBookByTitle(title: String): Book? {
        return bookDao.getBookByTitle(title)
    }

    override suspend fun getAuthorByName(name: String): Author? {
        return authorDao.getAuthorByName(name)
    }

    private suspend fun getOrCreateAuthorId(author: Author): Long {
        return if (author.id != 0L) {
            author.id
        } else {
            val existing = authorDao.getAuthorByName(author.name)
            existing?.id ?: authorDao.insertAuthor(author)
        }
    }

    private suspend fun getOrCreateGenreId(genre: Genre): Long {
        return if (genre.id != 0L) {
            genre.id
        } else {
            val existing = genreDao.getGenreByName(genre.name)
            existing?.id ?: genreDao.insertGenre(genre)
        }
    }

    private suspend fun getOrCreateTagId(tag: Tag): Long {
        return if (tag.id != 0L) {
            tag.id
        } else {
            val existing = tagDao.getTagByName(tag.name)
            existing?.id ?: tagDao.insertTag(tag)
        }
    }

    override suspend fun updateAuthor(author: Author) = authorDao.updateAuthor(author)
    override suspend fun deleteAuthor(author: Author) = authorDao.deleteAuthor(author)
    override suspend fun updateGenre(genre: Genre) = genreDao.updateGenre(genre)
    override suspend fun deleteGenre(genre: Genre) = genreDao.deleteGenre(genre)
    override suspend fun updateTag(tag: Tag) = tagDao.updateTag(tag)
    override suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)
}
