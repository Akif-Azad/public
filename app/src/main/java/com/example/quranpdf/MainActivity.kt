package com.example.quranpdf

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView

class MainActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var lastReadView: TextView
    private lateinit var lastReadStore: LastReadStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pdfView = findViewById(R.id.pdfView)
        lastReadView = findViewById(R.id.lastReadView)
        lastReadStore = LastReadStore(this)

        val initialPage = lastReadStore.getLastPage()

        pdfView.fromAsset("quran.pdf")
            .defaultPage(initialPage)
            .enableSwipe(true)
            .enableDoubletap(true)
            .enableAnnotationRendering(true)
            .enableAntialiasing(true)
            .enableRenderDuringScale(true)
            .onPageChange { page, _ ->
                lastReadStore.savePage(page)
                renderLastRead()
            }
            .load()

        renderLastRead()
    }

    private fun renderLastRead() {
        val history = lastReadStore.getHistory()
        if (history.isEmpty()) {
            lastReadView.text = "Last read: ابھی شروع نہیں کیا"
            return
        }

        val formatted = history.joinToString(separator = " | ") {
            "صفحہ ${it.page + 1} (${it.count}/7)"
        }
        lastReadView.text = "Last read (5 pages): $formatted"
    }
}
