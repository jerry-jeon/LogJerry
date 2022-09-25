import androidx.compose.ui.text.AnnotatedString
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import parse.DefaultParser

internal class ParserTest {

    @Test
    fun `Given raw login, parse well`() {
        val parseResult = DefaultParser().parse(
            listOf("2021-12-19 23:05:36.664 165-165/? I/hwservicemanager: Since android.hardware.media.omx@1.0::IOmxStore/default is not registered, trying to start it as a lazy HAL.")
        )

        parseResult shouldBe Log(
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
}
