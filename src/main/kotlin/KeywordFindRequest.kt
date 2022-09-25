sealed class KeywordFindRequest {
    class TurnedOn(val keyword: String) : KeywordFindRequest()
    object TurnedOff : KeywordFindRequest()
}
