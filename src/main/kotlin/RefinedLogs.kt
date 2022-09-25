data class RefinedLogs(
    val originalLogs: List<Log>,
    val refined: List<Log>,
    val detectionResults: Map<String, List<DetectionResult>>,
)
