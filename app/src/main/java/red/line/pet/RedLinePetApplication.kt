package red.line.pet

import android.app.Application
import red.line.pet.core.di.AppContainer
import red.line.pet.core.notification.PetoraNotificationManager

class RedLinePetApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        PetoraNotificationManager.createNotificationChannels(this)
    }
}
