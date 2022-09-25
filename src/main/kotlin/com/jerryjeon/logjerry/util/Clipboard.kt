package com.jerryjeon.logjerry.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun copyToClipboard(prettyJson: String) {
    val selection = StringSelection(prettyJson)
    Toolkit.getDefaultToolkit()
        .systemClipboard
        .setContents(selection, selection)
}
