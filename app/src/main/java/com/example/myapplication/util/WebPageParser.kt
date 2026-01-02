package com.example.myapplication.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParsedBookInfo(
    val title: String = "",
    val authors: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String = "",
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val sourceUrl: String = ""
) : Parcelable


object WebPageParser {

    suspend fun parseUrl(url: String): ParsedBookInfo = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get()

            if (url.contains("goodreads.com")) {
                parseGoodreads(doc, url)
            } else {
                parseGeneric(doc, url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ParsedBookInfo(sourceUrl = url)
        }
    }

    private fun parseGeneric(doc: Document, url: String): ParsedBookInfo {
        // Open Graph
        val ogTitle = doc.select("meta[property=og:title]").attr("content")
        val ogDesc = doc.select("meta[property=og:description]").attr("content")
        val ogImage = doc.select("meta[property=og:image]").attr("content")
        
        // Meta Tags fallback
        val metaTitle = doc.select("meta[name=title]").attr("content")
        val metaDesc = doc.select("meta[name=description]").attr("content")
        val metaAuthor = doc.select("meta[name=author]").attr("content")

        // Title
        var title = if (ogTitle.isNotBlank()) ogTitle else metaTitle
        if (title.isBlank()) title = doc.title()

        // Description
        val description = if (ogDesc.isNotBlank()) ogDesc else metaDesc

        // Authors
        val authors = mutableListOf<String>()
        if (metaAuthor.isNotBlank()) {
            authors.add(metaAuthor)
        }

        return ParsedBookInfo(
            title = title,
            authors = authors,
            description = description,
            imageUrl = ogImage,
            sourceUrl = url
        )
    }

    private fun parseGoodreads(doc: Document, url: String): ParsedBookInfo {
        // Goodreads often has specific classes or microdata
        // We can still use generic fallback if specific selectors fail, 
        // but let's try to target specific Goodreads elements based on common structure.
        
        // Title (often has id="bookTitle")
        var title = doc.select("h1#bookTitle").text().trim()
        if (title.isBlank()) {
           title = doc.select("h1[data-testid=bookTitle]").text().trim()
        }
        if (title.isBlank()) {
            // Fallback to OG
            title = doc.select("meta[property=og:title]").attr("content")
        }

        // Authors
        val authors = mutableListOf<String>()
        // Old design
        doc.select("a.authorName").forEach { element ->
            val name = element.text().trim()
            if (name.isNotBlank() && !authors.contains(name)) {
                authors.add(name)
            }
        }
        // New design (approximate)
        if (authors.isEmpty()) {
             doc.select("span.ContributorLink__name").forEach { element ->
                val name = element.text().trim()
                if (name.isNotBlank() && !authors.contains(name)) {
                    authors.add(name)
                }
            }
        }
        // Fallback meta
        if (authors.isEmpty()) {
            // There might be a meta author, but Goodreads often doesn't put all authors there
        }

        // Description
        var description = doc.select("div#description span[style*='display:none']").text().trim() // Full description often hidden
        if (description.isBlank()) {
             description = doc.select("div#description").text().trim()
        }
        if (description.isBlank()) {
             description = doc.select("div[data-testid=description]").text().trim()
        }
        if (description.isBlank()) {
            description = doc.select("meta[property=og:description]").attr("content")
        }

        // Genres (Goodreads specific)
        val genres = mutableListOf<String>()
        doc.select("a.actionLinkLite.bookPageGenreLink").forEach { 
            genres.add(it.text().trim())
        }
        if (genres.isEmpty()) {
             doc.select("ul.CollapsableList span.Button__labelItem").forEach {
                 genres.add(it.text().trim())
             }
        }

        // Image
        var imageUrl = doc.select("img#coverImage").attr("src")
        if (imageUrl.isBlank()) {
             imageUrl = doc.select("meta[property=og:image]").attr("content")
        }

        return ParsedBookInfo(
            title = title,
            authors = authors,
            description = description,
            imageUrl = imageUrl,
            genres = genres.take(3), // Take top 3 genres
            sourceUrl = url
        )
    }
}
