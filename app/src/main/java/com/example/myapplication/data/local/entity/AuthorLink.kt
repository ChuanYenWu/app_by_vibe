package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "author_links",
    foreignKeys = [
        ForeignKey(
            entity = Author::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("authorId")]
)
data class AuthorLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: Long,
    val linkText: String,
    val url: String
)
