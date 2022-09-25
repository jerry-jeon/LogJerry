package com.jerryjeon.logjerry.detection

sealed class KeywordDetectionRequest {
    class TurnedOn(val keyword: String) : KeywordDetectionRequest()
    object TurnedOff : KeywordDetectionRequest()
}
