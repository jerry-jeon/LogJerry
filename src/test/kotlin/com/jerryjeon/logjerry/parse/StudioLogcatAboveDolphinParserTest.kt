package com.jerryjeon.logjerry.parse

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class StudioLogcatAboveDolphinParserTest {

    @Nested
    inner class FactoryTest {

        @ParameterizedTest
        @MethodSource("logAndIncludeSettings")
        fun `Factory can be created for all include settings`(input: String, expected: StudioLogcatAboveDolphinParser) {
            val parser = StudioLogcatAboveDolphinParser.create(input)
            parser.shouldBeInstanceOf<StudioLogcatAboveDolphinParser>() shouldBe expected

            parser.parse(listOf(input)).invalidSentences.shouldBeEmpty()
        }

        // TODO add tests for various formats
        private fun logAndIncludeSettings(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "2022-10-30 20:23:38.484 19086-19156 Gralloc4                com.example.myapplication            I  mapper 4.x is not supported",
                    StudioLogcatAboveDolphinParser(
                        includeDate = true,
                        includeTime = true,
                        includePid = true,
                        includeTid = true,
                        includeTag = true,
                        includePackageName = true
                    )
                ),
                Arguments.of(
                    "2022-09-26 23:45:28.054   321-16228 resolv                  pid-321                              D  res_nmkquery: (QUERY, IN, A)",
                    StudioLogcatAboveDolphinParser(
                        includeDate = true,
                        includeTime = true,
                        includePid = true,
                        includeTid = true,
                        includeTag = true,
                        includePackageName = true
                    )
                ),
                Arguments.of(
                    "2022-09-26 23:46:30.454   543-599   VerityUtils             system_process                       E  Failed to measure fs-verity, errno 1: /data/app/~~byIkOUX0heIwtiImACXFMg==/com.sendbird.android.test-e2Yic0CCjvMq4lmW7z0IAA==/base.apk",
                    StudioLogcatAboveDolphinParser(
                        includeDate = true,
                        includeTime = true,
                        includePid = true,
                        includeTid = true,
                        includeTag = true,
                        includePackageName = true
                    )
                ),

                // Compact view
                Arguments.of(
                    "20:58:04.567  W  Failed to initialize 101010-2 format, error = EGL_SUCCESS",
                    StudioLogcatAboveDolphinParser(
                        includeDate = false,
                        includeTime = true,
                        includePid = false,
                        includeTid = false,
                        includeTag = false,
                        includePackageName = false
                    )
                ),

                Arguments.of(
                    "20:58:04.567 Tag W  Failed to initialize 101010-2 format, error = EGL_SUCCESS",
                    StudioLogcatAboveDolphinParser(
                        includeDate = false,
                        includeTime = true,
                        includePid = false,
                        includeTid = false,
                        includeTag = true,
                        includePackageName = false
                    )
                ),
            )
        }
    }
}