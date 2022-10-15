package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.detector.JsonDetector
import com.jerryjeon.logjerry.log.Log
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import org.junit.jupiter.api.Test

internal class JsonTest {

    @Test
    fun `Parse Json with {} in strings correctly`() {
        val parseResult = JsonDetector().detect(
            "{\"Name\":\"The Only Road (feat. Danyka Nadeau)\",\"ImageBlurHashes\":{\"Primary\":{\"dd58848b72bc9a8968d76eece457cab8\":\"eMN,bzs.tl?u8_~U-;-;%Ls:pJM{D%MxkD-.t7jbo2oz.8s:V@t6tQ\",\"66e4be9e3be3d61d751a31e7dbe93f84\":\"e65:=*sn10ja-TNvw_PW:S35m}sU^4bG9^,?SMJ8ays.XkWpIqWV=w\"}}}",1
        )
        assert(parseResult.size == 1)
    }
}
