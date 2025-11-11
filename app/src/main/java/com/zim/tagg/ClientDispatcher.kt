package com.zim.tagg

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.preference.PreferenceManager

object ClientDispatcher {
    fun dispatch(ctx: Context, item: ResultItem) {
        val magnet = item.magnet ?: item.torrentUrl
        if (magnet == null) {
            Toast.makeText(ctx, "No magnet/torrent available", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val preferredPkg = prefs.getString("preferredclientpkg", null)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(magnet)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (preferredPkg != null) setPackage(preferredPkg)
        }

        val pm = ctx.packageManager
        if (preferredPkg != null) {
            val canHandle = pm.queryIntentActivities(intent, 0).any { it.activityInfo.packageName == preferredPkg }
            if (canHandle) { ctx.startActivity(intent); return }
        }

        val anyHandler = pm.queryIntentActivities(Intent(Intent.ACTION_VIEW, Uri.parse("magnet:?xt=urn:btih:test")), 0)
            .firstOrNull()?.activityInfo?.packageName
        if (anyHandler != null) {
            intent.setPackage(anyHandler)
            ctx.startActivity(intent)
        } else {
            Toast.makeText(ctx, "No torrent download client installed", Toast.LENGTH_SHORT).show()
        }
    }
}
