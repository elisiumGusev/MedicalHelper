package com.llsparrow.healthassistant.core_base_impl.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.llsparrow.core_base_api.clipboard.Clipboard
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardImpl @Inject constructor(context: Context) : Clipboard {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override fun copy(text: String) {
        val clipData = ClipData.newPlainText("MedicalHelper", text)
        clipboardManager.setPrimaryClip(clipData)
    }
}