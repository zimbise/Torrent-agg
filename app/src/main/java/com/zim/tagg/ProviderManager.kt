package com.zim.tagg

import android.content.Context
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ProviderManager(private val ctx: Context) {
    private val baseRegistry = loadJsonAsset("registry/providers.json")
    private val headersProfiles = loadJsonAsset("proxy/headers.json")
    private val userRegistry = runCatching { loadUserFile("providers-user.json") }.getOrNull()

    private fun loadJsonAsset(path: String): JSONObject =
        JSONObject(ctx.assets.open(path).bufferedReader().readText())

    private fun loadUserFile(name: String): JSONObject =
        JSONObject(ctx.openFileInput(name).bufferedReader().readText())

    fun providers(): List<Provider> {
        val merged = JSONArray()
        val base = baseRegistry.getJSONArray("providers")
        for (i in 0 until base.length()) merged.put(base.getJSONObject(i))
        if (userRegistry != null) {
            val user = userRegistry.optJSONArray("providers") ?: JSONArray()
            for (i in 0 until user.length()) merged.put(user.getJSONObject(i))
        }
        val out = mutableListOf<Provider>()
        for (i in 0 until merged.length()) {
            val p = merged.getJSONObject(i)
            if (!p.optBoolean("enabled", true)) continue
            out += Provider(
                id = p.getString("id"),
                name = p.getString("name"),
                searchUrl = p.getString("searchUrl"),
                parser = p.getString("parser"),
                headersProfile = p.getString("headersProfile"),
                proxyProfile = p.getString("proxyProfile"),
                timeoutMs = p.optInt("timeoutMs", 12000)
            )
        }
        return out
    }

    suspend fun searchAll(query: String): List<ResultItem> = coroutineScope {
        providers().map { provider ->
            async(Dispatchers.IO) { searchProvider(provider, query) }
        }.flatMap { runCatching { it.await() }.getOrDefault(emptyList()) }
    }

    private fun applyHeaders(conn: HttpURLConnection, profile: String) {
        val prof = headersProfiles.getJSONObject(profile)
        prof.keys().forEach { k -> conn.setRequestProperty(k, prof.getString(k)) }
    }

    private fun baseFrom(url: URL) = "${url.protocol}://${url.host}"

    private fun encode(q: String) = URLEncoder.encode(q, "UTF-8")

    private fun fetch(url: String, timeout: Int, headersProfile: String): String {
        val u = URL(url)
        val conn = u.openConnection() as HttpURLConnection
        conn.connectTimeout = timeout
        conn.readTimeout = timeout
        applyHeaders(conn, headersProfile)
        return conn.inputStream.bufferedReader().readText()
    }

    private fun parserJson(path: String) = ctx.assets.open(path).bufferedReader().readText()

    private fun searchProvider(p: Provider, q: String): List<ResultItem> {
        val url = p.searchUrl.replace("{query}", encode(q))
        val html = fetch(url, p.timeoutMs, p.headersProfile)
        val cfg = parserJson(p.parser)
        return ParserEngine.parse(html, cfg, baseFrom(URL(url)), p.id)
    }
}
