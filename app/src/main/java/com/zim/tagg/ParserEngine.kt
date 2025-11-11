package com.zim.tagg

import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object ParserEngine {
    fun parse(html: String, cfgJson: String, base: String, providerId: String): List<ResultItem> {
        val cfg = JSONObject(cfgJson)
        val doc = Jsoup.parse(html, base)
        val rows = doc.select(cfg.getString("listSelector"))
        val out = mutableListOf<ResultItem>()

        for (el in rows) {
            val fields = cfg.getJSONObject("fields")
            val title = sel(el, fields.optJSONObject("title"))
            val detailUrl = sel(el, fields.optJSONObject("detailUrl"))
            val seeders = asInt(sel(el, fields.optJSONObject("seeders")))
            val leechers = asInt(sel(el, fields.optJSONObject("leechers")))
            val size = sel(el, fields.optJSONObject("size"))

            var magnet: String? = null
            var torrentUrl: String? = null

            val magnetCfg = cfg.optJSONObject("magnet")
            if (magnetCfg?.optBoolean("fromDetailPage", false) == true && detailUrl != null) {
                val dd = Jsoup.connect(detailUrl).get()
                magnet = dd.select(magnetCfg.optString("detailMagnetSelector", "a[href^='magnet:?']")).firstOrNull()?.attr("href")
                torrentUrl = dd.select(magnetCfg.optString("fallbackTorrentSelector", "a[href$='.torrent']")).firstOrNull()?.attr("href")
            } else {
                magnet = el.select("a[href^='magnet:?']").firstOrNull()?.attr("href")
                torrentUrl = el.select("a[href$='.torrent']").firstOrNull()?.attr("href")
            }

            if (title != null) {
                out += ResultItem(title, magnet, torrentUrl, detailUrl, seeders, leechers, size, providerId)
            }
        }
        return out
    }

    private fun sel(el: Element, cfg: JSONObject?): String? {
        if (cfg == null) return null
        val node = el.select(cfg.getString("selector")).firstOrNull() ?: return null
        return if (cfg.optString("attr", "text") == "text") node.text().trim() else node.attr(cfg.getString("attr")).trim()
    }

    private fun asInt(v: String?): Int? = v?.filter(Char::isDigit)?.toIntOrNull()
}
