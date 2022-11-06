package com.jerryjeon.logjerry.source

import com.jerryjeon.logjerry.log.Log
import kotlinx.coroutines.flow.StateFlow

sealed class Source {

    class ZipFile(val file: java.io.File) : Source()

    class File(val file: java.io.File) : Source()

    class Text(val text: String) : Source()

    class LogsFlow(val logs: StateFlow<List<Log>>) : Source()

    object None : Source()
}
