package com.zim.tagg

data class Provider(
    val id: String,
    val name: String,
    val searchUrl: String,
    val parser: String,
    val headersProfile: String,
    val proxyProfile: String,
    val timeoutMs: Int
)

data class ResultItem(
    val title: String,
    val magnet: String?,
    val torrentUrl: String?,
    val detailUrl: String?,
    val seeders: Int?,
    val leechers: Int?,
    val size: String?,
    val providerId: String
)
