package com.mod.paragraphapp

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import java.util.regex.Pattern

class MainActivityV2 : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var tts: TextToSpeech
    private var spannableContent: SpannableStringBuilder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        initViews()
        initTTS()
        initTextViewPlainText()
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

    private fun initTextViewPlainText() {
        val sentences = listOf(
            "Social capital, defined as the networks, norms, and trust that facilitate coordination and cooperation among individuals and groups, has gained prominence in discussions of economic development.",
            "Unlike physical or human capital, social capital is intangible but equally vital for fostering innovation, reducing transaction costs, and enhancing collective action.",
            "Communities with strong social capital often experience lower crime rates, better health outcomes, and more resilient economies.",
            "However, excessive reliance on tight-knit groups can also result in exclusion and inequality.",
            "Therefore, policymakers emphasize cultivating inclusive social networks that encourage both bonding and bridging ties to promote equitable growth and societal well-being."
        )

        val keywords = listOf(
            "social capital", "networks", "norms", "trust",
            "economic development", "physical", "human capital", "innovation",
            "transaction costs", "collective action", "crime rates", "health outcomes",
            "resilient economies", "exclusion", "inequality", "bonding", "bridging ties"
        )

        val fullText = sentences.joinToString(separator = " ")
        val builder = SpannableStringBuilder(fullText)
        spannableContent = builder

        keywords.forEach { keyword ->
            val pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(fullText)

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()

                builder.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        highlightSentencePerLine(start, end, Color.DKGRAY)
                        showTappedWordDialog(fullText.substring(start, end))
                        sayTheTappedWord(fullText.substring(start, end))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = ds.linkColor
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        var currentIndex = 0
        sentences.forEach { sentence ->
            val start = fullText.indexOf(sentence, currentIndex)
            if (start != -1) {
                val end = start + sentence.length

                builder.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        highlightSentencePerLine(start, end, Color.parseColor("#228B22"))
                        showTappedWordDialog(fullText.substring(start, end))
                        sayTheTappedWord(fullText.substring(start, end))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = ds.linkColor
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                currentIndex = end
            }
        }

        textView.text = builder
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
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

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
