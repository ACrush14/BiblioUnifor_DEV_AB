package com.example.bibliounifornew.data

data class GoogleBooksResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val categories: List<String>?,
    val industryIdentifiers: List<IndustryIdentifier>?,
    val imageLinks: ImageLinks?
)

data class IndustryIdentifier(
    val type: String?,
    val identifier: String?
)

data class ImageLinks(
    val thumbnail: String?
)