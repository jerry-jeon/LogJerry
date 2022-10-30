package com.jerryjeon.logjerry.source

import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.parse.LogParser
import com.jerryjeon.logjerry.parse.ParseStatus
import com.jerryjeon.logjerry.parse.StudioLogcatAboveDolphinParser
import com.jerryjeon.logjerry.parse.StudioLogcatBelowChipmunkParser
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.openZip

class SourceManager(private val preferences: Preferences) {
    private val sourceScope = CoroutineScope(Dispatchers.Default)
    private val studioLogcatBelowChipmunkParser = StudioLogcatBelowChipmunkParser(
        includeDateTime = true,
        includePidTid = true,
        includePackageName = true,
        includeTag = true
    )

    val sourceFlow: MutableStateFlow<Source> = MutableStateFlow(Source.None)
    val parseStatusFlow: StateFlow<ParseStatus> = sourceFlow.map {
        when (it) {
            is Source.ZipFile -> {
                val fileSystem =  FileSystem.SYSTEM
                val zipFileSystem = fileSystem.openZip(it.file.toOkioPath())
                val files = zipFileSystem.listOrNull("/".toPath()) ?: return@map ParseStatus.NotStarted
                val content = zipFileSystem.read(files.first()) { readUtf8() }.split("\n")
                val parser = chooseParser(content)
                val parseResult = parser.parse(content)
                ParseStatus.Completed(parseResult, LogManager(parseResult.logs, preferences))
            }

            is Source.File -> {
                val lines = it.file.readLines()
                val parser = chooseParser(lines)
                val parseResult = parser.parse(it.file.readLines())
                ParseStatus.Completed(parseResult, LogManager(parseResult.logs, preferences))
            }

            is Source.Text -> {
                val lines = it.text.split("\n")
                val parser = chooseParser(lines)
                val parseResult = parser.parse(lines)
                ParseStatus.Completed(parseResult, LogManager(parseResult.logs, preferences))
            }
            Source.None -> {
                ParseStatus.NotStarted
            }
        }
    }.stateIn(sourceScope, SharingStarted.Lazily, ParseStatus.NotStarted)

    private fun chooseParser(lines: List<String>): LogParser {
        // Prefer second line because the first line breaks often because of the buffer
        val sample = lines.getOrNull(1) ?: lines.first()

        return StudioLogcatBelowChipmunkParser.create(sample)
            ?: StudioLogcatAboveDolphinParser.create(sample)
            ?: studioLogcatBelowChipmunkParser
    }

    fun changeSource(source: Source) {
        this.sourceFlow.value = source
    }

    fun turnOnKeywordDetection() {
        when (val value = parseStatusFlow.value) {
            is ParseStatus.Completed -> {
                value.logManager.detectorManager.setKeywordDetectionEnabled(true)
            }
            ParseStatus.NotStarted -> {}
            is ParseStatus.Proceeding -> {}
        }
    }
}
