import androidx.compose.ui.text.AnnotatedString

object SampleData {
    val rawLog = "2021-12-19 23:05:36.664 165-165/? I/hwservicemanager: Since android.hardware.media.omx@1.0::IOmxStore/default is not registered, trying to start it as a lazy HAL."
    val log = Log(
        1,
        "2021-12-19",
        "23:05:36.664",
        165,
        165,
        null,
        "I",
        "hwservicemanager",
        "Since android.hardware.media.omx@1.0::IOmxStore/default is not registered, trying to start it as a lazy HAL.",
        AnnotatedString("")
    )
}
