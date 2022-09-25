package com.jerryjeon.logjerry.source

import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.parse.DefaultParser
import com.jerryjeon.logjerry.parse.ParseStatus
import com.jerryjeon.logjerry.transformation.TransformationManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SourceManager {
    private val sourceScope = MainScope()
    private val parser = DefaultParser()
    val sourceFlow: MutableStateFlow<Source> = MutableStateFlow(Source.None)
    val parseStatusFlow: StateFlow<ParseStatus> = sourceFlow.map {
        when (it) {
            is Source.File -> {
                val parseResult = parser.parse(it.file.readLines())
                val transformationManager = TransformationManager()
                ParseStatus.Completed(parseResult, transformationManager, LogManager(parseResult.logs, transformationManager))
            }

            is Source.Text -> {
                val parseResult = parser.parse(it.text.split("\n"))
                val transformationManager = TransformationManager()
                ParseStatus.Completed(parseResult, transformationManager, LogManager(parseResult.logs, transformationManager))
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
                value.transformationManager.setKeywordDetectionEnabled(true)
            }
            ParseStatus.NotStarted -> {}
            is ParseStatus.Proceeding -> {}
        }
    }
}
