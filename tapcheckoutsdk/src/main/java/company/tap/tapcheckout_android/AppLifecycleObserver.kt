package company.tap.tapcheckout_android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent


class AppLifecycleObserver : LifecycleObserver {


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onEnterForeground() {
        TapCheckoutDataConfiguration.getAppLifeCycle()?.onEnterForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        TapCheckoutDataConfiguration.getAppLifeCycle()?.onEnterBackground()
    }
}