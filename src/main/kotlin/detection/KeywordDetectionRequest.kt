package detection

sealed class KeywordDetectionRequest {
    class TurnedOn(val keyword: String) : KeywordDetectionRequest()
    object TurnedOff : KeywordDetectionRequest()
}
