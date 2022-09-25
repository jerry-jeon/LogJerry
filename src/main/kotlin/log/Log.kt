import androidx.compose.ui.text.AnnotatedString

data class Log(
    val number: Int,
    val date: String,
    val time: String,
    val pid: Long,
    val tid: Long,
    val packageName: String?,
    val priorityText: String,
    val tag: String,
    val originalLog: String,
    val log: AnnotatedString
) {
    val priority = Priority.find(priorityText)
}

enum class Priority(val text: String, val level: Int) {
    Verbose("V", 1),
    Debug("D", 2),
    Info("I", 3),
    Warning("W", 4),
    Error("E", 5);

    companion object {
        fun find(text: String): Priority {
            return values().find { it.text == text }
                ?: throw IllegalArgumentException("Not defined priority: $text")
        }
    }
}
