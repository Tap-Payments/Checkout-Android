/*
 * *
 *  * Created by AhlaamK on 10/26/23, 7:02 AM
 *  * Copyright (c) 2023 . All rights reserved Tap Payments.
 *  *
 *
 */

package company.tap.tapcheckout_android

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("company.tap.tappaybutton.test", appContext.packageName)
    }
}