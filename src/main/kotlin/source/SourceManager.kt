package source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import log.LogManager
import parse.DefaultParser
import parse.ParseStatus

class SourceManager {
    private val sourceScope = MainScope()
    private val parser = DefaultParser()
    val sourceFlow: MutableStateFlow<Source> = MutableStateFlow(Source.None)
    val parseStatusFlow: StateFlow<ParseStatus> = sourceFlow.map {
        when (it) {
            is Source.File -> {
                val parseResult = parser.parse(it.file.readLines())
                ParseStatus.Completed(parseResult, LogManager(parseResult.logs))
            }

            is Source.Text -> {
                val parseResult = parser.parse(it.text.split("\n"))
                ParseStatus.Completed(parseResult, LogManager(parseResult.logs))
            }
            Source.None -> {
                ParseStatus.NotStarted
            }
        }
    }.stateIn(sourceScope, SharingStarted.Lazily, ParseStatus.NotStarted)

    fun changeSource(source: Source) {
        this.sourceFlow.value = source
    }

    fun turnOnKeywordDetection() {
        when (val value = parseStatusFlow.value) {
            is ParseStatus.Completed -> {
                value.logManager.setKeywordDetectionEnabled(true)
            }
            ParseStatus.NotStarted -> {}
            is ParseStatus.Proceeding -> {}
        }
    }
}
