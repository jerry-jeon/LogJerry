package com.jerryjeon.logjerry.parse

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class StudioLogcatBelowChipmunkParserTest {

    @Nested
    inner class FactoryTest {

        @ParameterizedTest
        @MethodSource("logAndStudioLogcatBelowChipmunkParser")
        fun `Factory can be created for all include settings`(input: String, expected: StudioLogcatBelowChipmunkParser) {
            val parser = StudioLogcatBelowChipmunkParser.create(input)
            parser.shouldNotBeNull() shouldBe expected

            parser.parse(listOf(input)).invalidSentences.shouldBeEmpty()
        }

        private fun logAndStudioLogcatBelowChipmunkParser(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = false, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = false, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "? I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = false, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = false, includePackageName = true, includeTag = true)
                ),
                Arguments.of(
                    "178-178 I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = true, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "178-178 I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = true, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "178-178/? I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = true, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "178-178/? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = false, includePidTid = true, includePackageName = true, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 08:50:35.786 I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = false, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 08:50:35.786 I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = false, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 ? I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = false, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 ? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = false, includePackageName = true, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178 I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = true, includePackageName = false, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178 I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = true, includePackageName = false, includeTag = true)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178/? I: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = true, includePackageName = true, includeTag = false)
                ),
                Arguments.of(
                    "2022-10-24 09:31:55.786 178-178/? I/servicemanager: Tried to unregister apexservice, but there is about to be a client.",
                    StudioLogcatBelowChipmunkParser(includeDateTime = true, includePidTid = true, includePackageName = true, includeTag = true)
                ),
            )
        }
    }
}
