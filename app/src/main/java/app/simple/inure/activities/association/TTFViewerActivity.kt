package app.simple.inure.activities.association

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.MimeConstants
import app.simple.inure.constants.Quotes
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.TTFHelper
import app.simple.inure.util.TextViewUtils.toHtmlSpanned
import app.simple.inure.util.ViewUtils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TTFViewerActivity : BaseActivity() {

    private lateinit var fontEditText: EditText
    private lateinit var fontName: TextView

    private var color: Int? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_font_viewer)

        color = theme.resolveAttrColor(R.attr.colorAppAccent)

        fontEditText = findViewById(R.id.ttf_viewer)
        fontName = findViewById(R.id.ttf_name)

        uri = if (intent.type == MimeConstants.ttfType) {
            if (intent?.action == Intent.ACTION_SEND) {
                intent.parcelable(Intent.EXTRA_STREAM)
            } else {
                intent.data
            }
        } else {
            showWarning("ERR: invalid file type ${intent?.type!!}")
            return
        }

        fontName.text = kotlin.runCatching {
            DocumentFile.fromSingleUri(this, uri!!)!!.name
        }.getOrElse {
            getString(R.string.not_available)
        }

        lifecycleScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val typeFace = TTFHelper.getTTFFile(contentResolver.openInputStream(intent.data!!)!!,
                                                    applicationContext,
                                                    DocumentFile.fromSingleUri(applicationContext, uri!!)!!.name!!)

                withContext(Dispatchers.Main) {
                    fontEditText.setTypeface(typeFace, Typeface.NORMAL)
                    fontName.setTypeface(typeFace, Typeface.NORMAL)
                    fontEditText.setText(Quotes.quotes.random().replace("%%%", color!!.toHexColor()).toHtmlSpanned())
                    fontEditText.visible(true)
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    showError(it.stackTraceToString())
                }
            }
        }
    }
}
