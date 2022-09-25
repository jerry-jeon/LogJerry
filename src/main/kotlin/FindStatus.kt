sealed class FindStatus {
    class TurnedOn(val keyword: String, val currentPosition: Int, val totalCount: Int) : FindStatus()
    object TurnedOff : FindStatus()
}
