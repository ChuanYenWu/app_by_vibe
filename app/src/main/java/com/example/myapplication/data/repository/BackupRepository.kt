package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.BackupDao
import com.example.myapplication.data.model.BackupData
import com.example.myapplication.data.model.BackupRelationships
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BackupRepository(private val backupDao: BackupDao) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val backupData = BackupData(
            exportDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
            books = backupDao.getAllBooks(),
            authors = backupDao.getAllAuthors(),
            genres = backupDao.getAllGenres(),
            tags = backupDao.getAllTags(),
            bookLinks = backupDao.getAllBookLinks(),
            authorLinks = backupDao.getAllAuthorLinks(),
            relationships = BackupRelationships(
                bookAuthors = backupDao.getAllBookAuthorCrossRefs(),
                bookGenres = backupDao.getAllBookGenreCrossRefs(),
                bookTags = backupDao.getAllBookTagCrossRefs()
            )
        )
        gson.toJson(backupData)
    }

    suspend fun importFromJson(json: String, clearExisting: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            backupDao.restoreDatabase(
                books = backupData.books,
                authors = backupData.authors,
                genres = backupData.genres,
                tags = backupData.tags,
                bookLinks = backupData.bookLinks,
                authorLinks = backupData.authorLinks,
                bookAuthorCrossRefs = backupData.relationships.bookAuthors,
                bookGenreCrossRefs = backupData.relationships.bookGenres,
                bookTagCrossRefs = backupData.relationships.bookTags,
                clearExisting = clearExisting
            )
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
