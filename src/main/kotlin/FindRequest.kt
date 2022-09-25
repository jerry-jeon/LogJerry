sealed class FindRequest {
    class TurnedOn(val keyword: String) : FindRequest()
    object TurnedOff : FindRequest()
}
