package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_author_cross_ref",
    primaryKeys = ["bookId", "authorId"],
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Author::class, parentColumns = ["id"], childColumns = ["authorId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["bookId"]), Index(value = ["authorId"])]
)
data class BookAuthorCrossRef(
    val bookId: Long,
    val authorId: Long
)

@Entity(
    tableName = "book_genre_cross_ref",
    primaryKeys = ["bookId", "genreId"],
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Genre::class, parentColumns = ["id"], childColumns = ["genreId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["bookId"]), Index(value = ["genreId"])]
)
data class BookGenreCrossRef(
    val bookId: Long,
    val genreId: Long
)

@Entity(
    tableName = "book_tag_cross_ref",
    primaryKeys = ["bookId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["bookId"]), Index(value = ["tagId"])]
)
data class BookTagCrossRef(
    val bookId: Long,
    val tagId: Long
)
