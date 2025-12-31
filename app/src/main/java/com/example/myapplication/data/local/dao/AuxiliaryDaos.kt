package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.Author
import com.example.myapplication.data.local.entity.Genre
import com.example.myapplication.data.local.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {
    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAllAuthors(): Flow<List<Author>>
    
    @Query("SELECT * FROM authors WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAuthors(query: String): Flow<List<Author>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthor(author: Author): Long
    
    @Query("SELECT * FROM authors WHERE name = :name LIMIT 1")
    suspend fun getAuthorByName(name: String): Author?
}

@Dao
interface GenreDao {
    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun getAllGenres(): Flow<List<Genre>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenre(genre: Genre): Long
    
    @Query("SELECT * FROM genres WHERE name = :name LIMIT 1")
    suspend fun getGenreByName(name: String): Genre?
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long
    
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?
}
