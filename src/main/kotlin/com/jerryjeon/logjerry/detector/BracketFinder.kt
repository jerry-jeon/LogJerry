package com.jerryjeon.logjerry.detector

fun String.findBracketRanges(start: Char, end: Char): List<IntRange> {
    // Find bracket pairs, { } and check this is json or not
    val bracketRanges = mutableListOf<IntRange>()

    var startIndex = -1
    var braceCount = 0
    var isInString = false
    var isEscaped = false

    var currentIndex = 0
    while (currentIndex < this.length) {
        val currentChar = this[currentIndex]
        if (!isEscaped) {
            if (currentChar == start && !isInString) {
                if (startIndex == -1) {
                    startIndex = currentIndex
                }
                braceCount++
            } else if (currentChar == end && !isInString) {
                braceCount--
                if (braceCount == 0 && startIndex != -1) {
                    val endIndex = currentIndex
                    bracketRanges.add(IntRange(startIndex, endIndex))
                    startIndex = -1
                }
            } else if (currentChar == '"') {
                if (currentIndex > 0) {
                    isInString = !isInString
                }
            }
        }
        isEscaped = currentChar == '\\' && !isEscaped
        currentIndex++
    }

    return bracketRanges
}
