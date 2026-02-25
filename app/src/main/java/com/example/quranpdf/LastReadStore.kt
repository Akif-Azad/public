package com.example.quranpdf

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "quran_reader_prefs"
private const val KEY_LAST_PAGE = "last_page"
private const val KEY_LAST_READ_HISTORY = "last_read_history"
private const val MAX_HISTORY_PAGES = 5
private const val MAX_REPEAT_COUNT = 7

data class LastReadEntry(val page: Int, val count: Int)

class LastReadStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastPage(): Int = prefs.getInt(KEY_LAST_PAGE, 0)

    fun savePage(page: Int) {
        prefs.edit().putInt(KEY_LAST_PAGE, page).apply()
        updateHistory(page)
    }

    fun getHistory(): List<LastReadEntry> {
        val raw = prefs.getString(KEY_LAST_READ_HISTORY, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                add(
                    LastReadEntry(
                        page = obj.optInt("page", 0),
                        count = obj.optInt("count", 1)
                    )
                )
            }
        }
    }

    private fun updateHistory(page: Int) {
        val current = getHistory().toMutableList()
        val idx = current.indexOfFirst { it.page == page }

        if (idx >= 0) {
            val existing = current.removeAt(idx)
            current.add(0, existing.copy(count = (existing.count + 1).coerceAtMost(MAX_REPEAT_COUNT)))
        } else {
            current.add(0, LastReadEntry(page = page, count = 1))
            if (current.size > MAX_HISTORY_PAGES) {
                current.removeLast()
            }
        }

        val output = JSONArray()
        current.forEach {
            output.put(
                JSONObject().apply {
                    put("page", it.page)
                    put("count", it.count)
                }
            )
        }

        prefs.edit().putString(KEY_LAST_READ_HISTORY, output.toString()).apply()
    }
}
