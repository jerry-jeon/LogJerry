package com.jerryjeon.logjerry.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun copyToClipboard(text: String) {
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit()
        .systemClipboard
        .setContents(selection, selection)
}
