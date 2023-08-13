package com.jerryjeon.logjerry.log

sealed class LogContent(val text: String) {
    class Text(text: String) : LogContent(text)
    class Json(text: String) : LogContent(text)

    class DataClass(text: String) : LogContent(text)
}
