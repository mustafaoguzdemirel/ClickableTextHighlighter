package com.mod.paragraphapp

//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        val textView = findViewById<TextView>(R.id.textView)
//
//
//        val fullText = "Bu bir ornek metindir. Bu metnin icinde bazı onemli kelimeler gecmektedir. Kelimeye veya cumleye tıklayabilirsiniz."
//        val sentenceList = listOf(
//            "Bu bir ornek metindir.",
//            "Bu metnin icinde bazı onemli kelimeler gecmektedir.",
//            "Kelimeye veya cumleye tıklayabilirsiniz."
//        )
//        val keywordList = listOf("ornek", "onemli", "kelimeler")
//
//        val manager = InteractiveTextManager(
//            textView = textView,
//            fullText = fullText,
//            sentences = sentenceList,
//            highlightedWords = keywordList
//        )
//        manager.render()
//    }
//
//    class InteractiveTextManager(
//        private val textView: TextView,
//        private val fullText: String,
//        private val sentences: List<String>,
//        private val highlightedWords: List<String>
//    ) {
//        private var selectedSentenceStartIndex: Int? = null
//        private var selectedWordStartIndex: Int? = null
//
//        fun render() {
//            val spannable = SpannableString(fullText)
//
//            // 🔹 Cümleler
//            for (sentence in sentences) {
//                val start = fullText.indexOf(sentence)
//                if (start != -1) {
//                    spannable.setSpan(object : ClickableSpan() {
//                        override fun onClick(widget: View) {
//                            selectedSentenceStartIndex = if (selectedSentenceStartIndex == start) null else start
//                            selectedWordStartIndex = null
//                            render()
//                        }
//
//                        override fun updateDrawState(ds: TextPaint) {
//                            ds.bgColor = if (start == selectedSentenceStartIndex) Color.YELLOW else Color.TRANSPARENT
//                            ds.color = Color.BLACK
//                            ds.isUnderlineText = false
//                        }
//                    }, start, start + sentence.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
//            }
//
//            // 🔹 Kelimeler
//            for (word in highlightedWords) {
//                var searchStartIndex = 0
//                while (true) {
//                    val start = fullText.indexOf(word, searchStartIndex)
//                    if (start == -1) break
//
//                    val end = start + word.length
//
//                    spannable.setSpan(object : ClickableSpan() {
//                        override fun onClick(widget: View) {
//                            selectedWordStartIndex = if (selectedWordStartIndex == start) null else start
//                            selectedSentenceStartIndex = null
//                            render()
//                        }
//
//                        override fun updateDrawState(ds: TextPaint) {
//                            val isWordSelected = start == selectedWordStartIndex
//
//                            val isInSelectedSentence = selectedSentenceStartIndex?.let { sentenceStart ->
//                                val sentence = sentences.find { fullText.indexOf(it) == sentenceStart }
//                                if (sentence != null) {
//                                    val sentenceEnd = sentenceStart + sentence.length
//                                    start >= sentenceStart && end <= sentenceEnd
//                                } else false
//                            } ?: false
//
//                            val bgColor = when {
//                                isWordSelected -> Color.GREEN
//                                isInSelectedSentence -> Color.YELLOW
//                                else -> Color.TRANSPARENT
//                            }
//
//                            ds.bgColor = bgColor
//                            ds.color = Color.BLACK
//                            ds.isUnderlineText = false
//                        }
//                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                    searchStartIndex = end
//                }
//            }
//
//
//            textView.text = spannable
//            textView.movementMethod = LinkMovementMethod.getInstance()
//        }
//
//    }
//
//}

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

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
        val spanned = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)
        spannableContent = SpannableStringBuilder(spanned)

        setSpannableContent()
        textView.text = spannableContent
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun setSpannableContent() {
        val links = spannableContent?.getSpans(
            0,
            spannableContent!!.length,
            android.text.style.URLSpan::class.java
        )

        links?.forEach { span ->
            val start = spannableContent!!.getSpanStart(span)
            val end = spannableContent!!.getSpanEnd(span)
            val visibleText = spannableContent!!.subSequence(start, end)

            spannableContent!!.removeSpan(span)

            spannableContent!!.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Eski rounded span'ları temizle
                    val oldSpans = spannableContent!!.getSpans(
                        0,
                        spannableContent!!.length,
                        RoundedBackgroundSpan::class.java
                    )
                    oldSpans.forEach { spannableContent!!.removeSpan(it) }

                    // Yeni rounded span uygula
                    spannableContent!!.setSpan(
                        RoundedBackgroundSpan(
                            bgColor = Color.DKGRAY,
                            textColor = Color.WHITE,
                            cornerRadius = 24f,
                            paddingHorizontal = 16f,
                            paddingVertical = 6f
                        ),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // UI güncelle
                    textView.text = spannableContent

                    // Dialog göster
                    showTappedWordDialog(visibleText.toString())
                    sayTheTappedWord(visibleText.toString())
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false // altı çizili olmasın
                    ds.color = ds.linkColor     // default link rengi kalabilir
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            .setTitle("Tıklanan Kelime")
            .setMessage(word)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun getHtmlContent(): String {
        return """
            <p>
              <a href="#" style="color: #2a9d8f;">Social capital</a>, defined as the <a href="#" style="color: #e76f51;">networks</a>, <a href="#" style="color: #f4a261;">norms</a>, and <a href="#" style="color: #e63946;">trust</a> that facilitate coordination and
              cooperation among individuals and groups, has gained prominence in discussions of
              <a href="#" style="color: #264653;">economic development</a>. Unlike <a href="#" style="color: #8d99ae;">physical</a> or <a href="#" style="color: #8d99ae;">human capital</a>, social capital is intangible but
              equally vital for fostering <a href="#" style="color: #457b9d;">innovation</a>, reducing <a href="#" style="color: #2b2d42;">transaction costs</a>, and enhancing <a href="#" style="color: #a8dadc;">collective action</a>. Communities with strong social capital often experience lower <a href="#" style="color: #e63946;">crime rates</a>, better <a href="#" style="color: #2a9d8f;">health outcomes</a>, and more resilient economies. However, excessive reliance on tight-knit
              groups can also result in <a href="#" style="color: #e9c46a;">exclusion</a> and <a href="#" style="color: #e9c46a;">inequality</a>. Therefore, policymakers emphasize
              cultivating inclusive social networks that encourage both <a href="#" style="color: #f4a261;">bonding</a> and <a href="#" style="color: #f4a261;">bridging ties</a> to
              promote equitable growth and societal well-being.
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
}

