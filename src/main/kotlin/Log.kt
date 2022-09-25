data class Log(
    val number: Int,
    val date: String,
    val time: String,
    val pid: Long,
    val tid: Long,
    val packageName: String?,
    val priority: String,
    val tag: String,
    val log: String
)
