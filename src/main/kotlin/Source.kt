sealed class Source {

    class File(val file: java.io.File) : Source()

    class Text(val text: String) : Source()

    object None: Source()

}
