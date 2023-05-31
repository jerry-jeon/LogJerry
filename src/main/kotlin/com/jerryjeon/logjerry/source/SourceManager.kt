package com.jerryjeon.logjerry.source

import com.jerryjeon.logjerry.log.ParseCompleted
import com.jerryjeon.logjerry.parse.*
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.openZip

class SourceManager(
    private val preferences: Preferences,
    initialSource: Source = Source.None
) {
    private val sourceScope = CoroutineScope(Dispatchers.Default)
    private val studioLogcatBelowChipmunkParser = StudioLogcatBelowChipmunkParser(
        includeDateTime = true,
        includePidTid = true,
        includePackageName = true,
        includeTag = true
    )

    val sourceFlow: MutableStateFlow<Source> = MutableStateFlow(initialSource)
    val parseStatusFlow: StateFlow<ParseStatus> = sourceFlow.map {
        if (it is Source.LogsFlow) {
            return@map ParseStatus.Completed(
                ParseResult(it.logs.value, emptyList()),
                ParseCompleted(it.logs, preferences)
            )
        }

        @Suppress("KotlinConstantConditions") val lines = when (it) {
            is Source.ZipFile -> {
                val zipFileSystem = FileSystem.SYSTEM.openZip(it.file.toOkioPath())
                val files = zipFileSystem.listOrNull("/".toPath()) ?: return@map ParseStatus.NotStarted
                zipFileSystem.read(files.first()) { readUtf8() }.split("\n")
            }

            is Source.File -> it.file.readLines()
            is Source.Text -> it.text.split("\n")
            is Source.LogsFlow -> throw IllegalStateException("Shouldn't reach here")
            Source.None -> return@map ParseStatus.NotStarted
        }
        val parser = chooseParser(lines)
        val parseResult = parser.parse(lines)
        ParseStatus.Completed(parseResult, ParseCompleted(MutableStateFlow(parseResult.logs), preferences))
    }.stateIn(sourceScope, SharingStarted.Lazily, ParseStatus.NotStarted)

    private fun chooseParser(lines: List<String>): LogParser {
        return lines.firstNotNullOfOrNull {
            StudioLogcatBelowChipmunkParser.create(it)
                ?: StudioLogcatAboveDolphinParser.create(it)
        } ?: studioLogcatBelowChipmunkParser // TODO would be better if show failure message that the parser doesn't exist that can parse the content
    }

    fun changeSource(source: Source) {
        this.sourceFlow.value = source
    }

    fun turnOnKeywordDetection() {
        when (val value = parseStatusFlow.value) {
            is ParseStatus.Completed -> {
                value.parseCompleted.detectorManager.setKeywordDetectionEnabled(true)
            }
            ParseStatus.NotStarted -> {}
            is ParseStatus.Proceeding -> {}
        }
    }
}
