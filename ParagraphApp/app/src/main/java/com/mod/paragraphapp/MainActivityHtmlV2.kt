package com.mod.paragraphapp

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.xml.sax.XMLReader
import java.util.*

class MainActivityHtmlV2 : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var tts: TextToSpeech
    private var spannableContent: SpannableStringBuilder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        initViews()
        initTTS()
        initTextView()
        setupButton()
    }

    private fun initViews() {
        textView = findViewById(R.id.textView)
        button = findViewById(R.id.button)
    }

    private fun initTTS() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Dil desteklenmiyor", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Text to Speech başlatılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initTextView() {
        val htmlContent = getHtmlContent()
        val spanned = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, null, SentenceTagHandler())
        spannableContent = SpannableStringBuilder(spanned)

        setSpannableContent()
        textView.text = spannableContent
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun setSpannableContent() {
        spannableContent?.let { builder ->

            // Önce tüm URLSpan (keyword'ler) işle
            val urlSpans = builder.getSpans(0, builder.length, URLSpan::class.java)
            urlSpans?.forEach { span ->
                val start = builder.getSpanStart(span)
                val end = builder.getSpanEnd(span)
                val visibleText = builder.subSequence(start, end)
                builder.removeSpan(span)

                builder.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        clearBackgroundSpans()
                        highlightSentencePerLine(start, end, Color.DKGRAY)
                        showTappedWordDialog(visibleText.toString())
                        sayTheTappedWord(visibleText.toString())
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                        ds.color = ds.linkColor
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // Sonra tüm SentenceSpan (cümleler) işle
            val sentenceSpans = builder.getSpans(0, builder.length, SentenceSpan::class.java)
            sentenceSpans?.forEach { span ->
                val start = builder.getSpanStart(span)
                val end = builder.getSpanEnd(span)
                val visibleText = builder.subSequence(start, end)
                builder.removeSpan(span)

                builder.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        clearBackgroundSpans()
                        highlightSentencePerLine(start, end, Color.parseColor("#228B22"))
                        showTappedWordDialog(visibleText.toString())
                        sayTheTappedWord(visibleText.toString())
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                        ds.color = ds.linkColor
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun highlightSpan(start: Int, end: Int, color: Int) {
        spannableContent?.let { builder ->
            builder.setSpan(
                RoundedBackgroundReplacementSpan(
                    bgColor = color,
                    textColor = Color.WHITE,
                    cornerRadius = 24f,
                    paddingHorizontal = 16f,
                    paddingVertical = 8f
                ),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = builder
        }
    }

    private fun clearBackgroundSpans() {
        spannableContent?.let { builder ->
            val spans = builder.getSpans(0, builder.length, RoundedBackgroundReplacementSpan::class.java)
            spans.forEach { builder.removeSpan(it) }
        }
    }

    private fun setupButton() {
        button.setOnClickListener {
            val text = textView.text.toString()
            if (tts.isSpeaking) {
                tts.stop()
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
            }
        }
    }

    private fun sayTheTappedWord(word: String) {
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
    }

    private fun showTappedWordDialog(word: String) {
        AlertDialog.Builder(this)
            .setTitle("Tıklanan İçerik")
            .setMessage(word)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun getHtmlContent(): String {
        return """
            <p>
                <sentence><a href="#">Social capital</a>, defined as the <a href="#">networks</a>, norms, and trust that facilitate coordination and cooperation among individuals and groups.</sentence>
                <sentence>Communities with strong <a href="#">social capital</a> often experience lower <a href="#">crime rates</a> and better <a href="#">health outcomes</a>.</sentence>
                <sentence>However, excessive reliance on tight-knit groups can also result in <a href="#">exclusion</a> and <a href="#">inequality</a>.</sentence>
            </p>
        """.trimIndent()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    // Custom <sentence> tag'ını işleyen TagHandler
    inner class SentenceTagHandler : Html.TagHandler {
        override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
            if (tag.equals("sentence", ignoreCase = true)) {
                if (opening) {
                    startSentence(output)
                } else {
                    endSentence(output)
                }
            }
        }

        private fun startSentence(text: Editable) {
            text.setSpan(StartSentenceMarker(), text.length, text.length, Spannable.SPAN_MARK_MARK)
        }

        private fun endSentence(text: Editable) {
            val obj = getLast(text, StartSentenceMarker::class.java)
            val where = text.getSpanStart(obj)
            text.removeSpan(obj)

            if (where != -1) {
                text.setSpan(SentenceSpan(), where, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        private fun <T> getLast(text: Spanned, kind: Class<T>): Any? {
            val objs = text.getSpans(0, text.length, kind)
            return if (objs.isEmpty()) null else objs[objs.size - 1]
        }
    }

    // Başlangıç markerı
    class StartSentenceMarker

    // Gerçek Sentence span
    class SentenceSpan

    private fun highlightSentencePerLine(start: Int, end: Int, color: Int) {
        spannableContent?.let { builder ->
            val layout = textView.layout ?: return

            val startLine = layout.getLineForOffset(start)
            val endLine = layout.getLineForOffset(end)

            clearBackgroundSpans()

            if (startLine == endLine) {
                builder.setSpan(
                    RoundedBackgroundReplacementSpan(
                        bgColor = color,
                        textColor = Color.WHITE,
                        cornerRadius = 24f,
                        paddingHorizontal = 16f,
                        paddingVertical = 8f
                    ),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                for (line in startLine..endLine) {
                    val lineStart = layout.getLineStart(line)
                    val lineEnd = layout.getLineEnd(line)

                    val spanStart = maxOf(start, lineStart)
                    val spanEnd = minOf(end, lineEnd)

                    builder.setSpan(
                        RoundedBackgroundReplacementSpan(
                            bgColor = color,
                            textColor = Color.WHITE,
                            cornerRadius = 24f,
                            paddingHorizontal = 16f,
                            paddingVertical = 8f
                        ),
                        spanStart,
                        spanEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            textView.text = builder
        }
    }

}
