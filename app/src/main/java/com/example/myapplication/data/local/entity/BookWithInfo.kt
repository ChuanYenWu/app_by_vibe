package com.example.myapplication.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookWithInfo(
    @Embedded val book: Book,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookAuthorCrossRef::class,
            parentColumn = "bookId",
            entityColumn = "authorId"
        )
    )
    val authors: List<Author>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookGenreCrossRef::class,
            parentColumn = "bookId",
            entityColumn = "genreId"
        )
    )
    val genres: List<Genre>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookTagCrossRef::class,
            parentColumn = "bookId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val links: List<BookLink>
)
