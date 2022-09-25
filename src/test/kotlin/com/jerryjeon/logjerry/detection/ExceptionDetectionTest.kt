package com.jerryjeon.logjerry.detection

import org.junit.jupiter.api.Test

internal class ExceptionDetectionTest {

    private val detection = ExceptionDetection()
    private val originalExceptionLog = """
        2022-08-20 11:39:45.117 5923-5944/com.sendbird.android.test E/TestRunner: java.lang.RuntimeException: asdfasdf
        at com.sendbird.android.MessageCollectionTest.test_hasNextFalse_otherUserSendMessage_OnlyMessageAddedCalledOnce(MessageCollectionTest.kt:54)
        at java.lang.reflect.Method.invoke(Native Method)
        at org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:50)
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
        at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
        at androidx.test.internal.runner.junit4.statement.RunBefores.evaluate(RunBefores.java:80)
        at androidx.test.internal.runner.junit4.statement.RunAfters.evaluate(RunAfters.java:61)
        at androidx.test.rule.ActivityTestRule${'$'}ActivityStatement.evaluate(ActivityTestRule.java:549)
        at org.junit.rules.TestWatcher${'$'}1.evaluate(TestWatcher.java:55)
        at org.junit.rules.RunRules.evaluate(RunRules.java:20)
        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
        at org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)
        at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:162)
        at org.junit.runners.Suite.runChild(Suite.java:128)
        at org.junit.runners.Suite.runChild(Suite.java:27)
        at org.junit.runners.ParentRunner${'$'}3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner${'$'}1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access${'$'}000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner${'$'}2.evaluate(ParentRunner.java:268)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
        at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:56)
        at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:444)
    """.trimIndent()

    @Test
    fun testExceptionDetection() {
        println("abc".substring(0, 1))
        println("abc".substring(1, 1))
    }
}
