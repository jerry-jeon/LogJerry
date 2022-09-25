class Parser {
    fun parse(raw: String): Log {
        val split = raw.split(" ")

        val date = split[0]
        val time = split[1]

        val thirdSegment = split[2].split("-", "/")
        val pid = thirdSegment[0].toLong()
        val tid = thirdSegment[1].toLong()
        val packageName = thirdSegment[2].takeIf { it != "?" }

        val fourthSegment = split[3].split("/")
        val priority = fourthSegment[0]
        val tag = fourthSegment[1].removeSuffix(":")

        val log = split.subList(4, split.size).joinToString(separator = " ")

        return Log(date, time, pid, tid, packageName, priority, tag, log)
    }
}
