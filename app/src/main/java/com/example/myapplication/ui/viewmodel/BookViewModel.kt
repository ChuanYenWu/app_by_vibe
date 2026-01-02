package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.BookApplication
import com.example.myapplication.data.local.entity.*
import com.example.myapplication.data.repository.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import com.example.myapplication.data.repository.SortOrder

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    // Filter States
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _filterReadingStatus = MutableStateFlow<String?>(null)
    val filterReadingStatus: StateFlow<String?> = _filterReadingStatus

    private val _filterAuthorIds = MutableStateFlow<List<Long>>(emptyList())
    val filterAuthorIds: StateFlow<List<Long>> = _filterAuthorIds

    private val _filterGenreIds = MutableStateFlow<List<Long>>(emptyList())
    val filterGenreIds: StateFlow<List<Long>> = _filterGenreIds

    private val _filterTagIds = MutableStateFlow<List<Long>>(emptyList())
    val filterTagIds: StateFlow<List<Long>> = _filterTagIds

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allBooks: StateFlow<List<BookWithInfo>> = combine(
        listOf(
            _sortOrder,
            _filterReadingStatus,
            _filterAuthorIds,
            _filterGenreIds,
            _filterTagIds,
            _searchQuery
        )
    ) { args ->
        val sort = args[0] as SortOrder
        val status = args[1] as String?
        @Suppress("UNCHECKED_CAST")
        val authors = args[2] as List<Long>
        @Suppress("UNCHECKED_CAST")
        val genres = args[3] as List<Long>
        @Suppress("UNCHECKED_CAST")
        val tags = args[4] as List<Long>
        val query = args[5] as String
        
        repository.getBooksByFilter(sort, status, authors, genres, tags, query)
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterReadingStatus(status: String?) { _filterReadingStatus.value = status }
    fun setFilterAuthors(ids: List<Long>) { _filterAuthorIds.value = ids }
    fun setFilterGenres(ids: List<Long>) { _filterGenreIds.value = ids }
    fun setFilterTags(ids: List<Long>) { _filterTagIds.value = ids }
    fun clearFilters() {
        _filterReadingStatus.value = null
        _filterAuthorIds.value = emptyList()
        _filterGenreIds.value = emptyList()
        _filterTagIds.value = emptyList()
    }
    
    val allAuthors: StateFlow<List<Author>> = repository.allAuthors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenres: StateFlow<List<Genre>> = repository.allGenres
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<Tag>> = repository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBook(
        title: String,
        readingStatus: String,
        rating: Float?,
        description: String?,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        links: List<BookLink>
    ) {
        viewModelScope.launch {
            val book = Book(
                title = title,
                readingStatus = readingStatus,
                rating = rating,
                description = description
            )
            repository.insertBook(book, authors, genres, tags, links)
        }
    }
    
    fun updateBook(
        id: Long,
        title: String,
        readingStatus: String,
        rating: Float?,
        description: String?,
        authors: List<Author>,
        genres: List<Genre>,
        tags: List<Tag>,
        links: List<BookLink>
    ) {
        viewModelScope.launch {
            val book = Book(
                id = id,
                title = title,
                readingStatus = readingStatus,
                rating = rating,
                description = description
            )
            repository.updateBook(book, authors, genres, tags, links)
        }
    }
    
    suspend fun checkDuplicateBook(title: String): Book? {
        return repository.getBookByTitle(title)
    }

    fun getBookById(id: Long) = repository.getBookById(id)

    // Entity Management Methods
    fun updateAuthor(author: Author) { viewModelScope.launch { repository.updateAuthor(author) } }
    fun deleteAuthor(author: Author) { viewModelScope.launch { repository.deleteAuthor(author) } }
    fun updateGenre(genre: Genre) { viewModelScope.launch { repository.updateGenre(genre) } }
    fun deleteGenre(genre: Genre) { viewModelScope.launch { repository.deleteGenre(genre) } }
    fun updateTag(tag: Tag) { viewModelScope.launch { repository.updateTag(tag) } }
    fun deleteTag(tag: Tag) { viewModelScope.launch { repository.deleteTag(tag) } }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            // We need to fetch the book first to delete it because the DAO requires the entity
            // In a real app we might want to optimize this to delete by ID directly in DAO
            repository.getBookById(bookId).firstOrNull()?.let { bookWithInfo ->
                 repository.deleteBook(bookWithInfo.book)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookApplication)
                val repository = application.container.bookRepository
                BookViewModel(repository)
            }
        }
    }
}
