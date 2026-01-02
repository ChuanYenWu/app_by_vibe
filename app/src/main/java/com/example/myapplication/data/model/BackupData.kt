package com.example.myapplication.data.model

import com.example.myapplication.data.local.entity.*

data class BackupData(
    val version: String = "1.0",
    val exportDate: String,
    val books: List<Book>,
    val authors: List<Author>,
    val genres: List<Genre>,
    val tags: List<Tag>,
    val bookLinks: List<BookLink>,
    val authorLinks: List<AuthorLink>,
    val relationships: BackupRelationships
)

data class BackupRelationships(
    val bookAuthors: List<BookAuthorCrossRef>,
    val bookGenres: List<BookGenreCrossRef>,
    val bookTags: List<BookTagCrossRef>
)
