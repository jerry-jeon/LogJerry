package com.jerryjeon.logjerry.parse

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class DefaultParserTest {

    @Nested
    inner class FactoryTest {

        @ParameterizedTest
        @MethodSource("logAndIncludeSettings")
        fun `Factory can be created for all include settings`(input: String, expected: IncludeSettings) {
            val parser = DefaultParser.create(input)
            parser.shouldBeInstanceOf<DefaultParser>()
                .asClue {
                    it.includeDateTime shouldBe expected.includeDateTime
                    it.includePidTid shouldBe expected.includePidTid
                    it.includePackageName shouldBe expected.includePackageName
                    it.includeTag shouldBe expected.includeTag
                }

            parser.parse(listOf(input)).invalidSentences.shouldBeEmpty()
        }

        private fun logAndIncludeSettings(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = false, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = false, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "? I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = false, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = false, includePackageName = true, includeTag = true)
                ),
                Arguments.of(
                    "178-178 I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = true, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "178-178 I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = true, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "178-178/? I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = true, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "178-178/? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = false, includePidTid = true, includePackageName = true, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 08:50:35.786 I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = false, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 08:50:35.786 I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = false, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 ? I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = false, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 ? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = false, includePackageName = true, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178 I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = true, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178 I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = true, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178/? I: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = true, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178/? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    IncludeSettings(includeDateTime = true, includePidTid = true, includePackageName = true, includeTag = true)
                ),
            )
        }
    }

    data class IncludeSettings(
        val includeDateTime: Boolean,
        val includePidTid: Boolean,
        val includePackageName: Boolean,
        val includeTag: Boolean,
    )
}
