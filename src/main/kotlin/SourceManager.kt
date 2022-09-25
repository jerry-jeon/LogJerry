import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import parse.DefaultParser

class SourceManager {
    private val parser = DefaultParser()
    val sourceFlow: MutableStateFlow<Source> = MutableStateFlow(Source.None)
    val parseStatusFlow: Flow<ParseStatus> = sourceFlow.map {
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
    }

    fun changeSource(source: Source) {
        this.sourceFlow.value = source
    }
}
