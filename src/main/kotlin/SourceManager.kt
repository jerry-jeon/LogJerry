import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import parse.DefaultParser

class SourceManager {
    val sourceFlow: MutableStateFlow<Source> = MutableStateFlow(Source.None)
    val parseStatusFlow: Flow<ParseStatus>
    val parser = DefaultParser()

    init {
        parseStatusFlow = sourceFlow.map {
            when (it) {
                is Source.File -> {
                    val parseResult = parser.parse(it.file.readLines())
                    ParseStatus.Completed(parseResult, LogRefinement(parseResult.logs))
                }

                is Source.Text -> {
                    val parseResult = parser.parse(it.text.split("\n"))
                    ParseStatus.Completed(parseResult, LogRefinement(parseResult.logs))
                }
                Source.None -> {
                    ParseStatus.NotStarted
                }
            }
        }
    }

    fun changeSource(source: Source) {
        this.sourceFlow.value = source
    }
}
