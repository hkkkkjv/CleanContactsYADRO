package ru.kpfu.itis.cleancontacts.data.bridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus
import ru.kpfu.itis.cleancontacts.service.ContactDedupService
import ru.kpfu.itis.cleancontacts.service.aidl.IContactService
import ru.kpfu.itis.cleancontacts.service.aidl.IStatusCallback
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AidlServiceBridge @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun removeDuplicates(): DedupStatus = suspendCancellableCoroutine { cont ->
        var connection: ServiceConnection? = null
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = IContactService.Stub.asInterface(binder) ?: run {
                    if (cont.isActive) cont.resume(DedupStatus.ERROR)
                    cleanup(this)
                    return
                }

                try {
                    val deathRecipient = object : IBinder.DeathRecipient {
                        override fun binderDied() {
                            if (cont.isActive) cont.resume(DedupStatus.ERROR)
                            cleanup(connection)
                            binder?.unlinkToDeath(this, 0)
                        }
                    }

                    binder?.linkToDeath(deathRecipient, 0)

                    service.removeDuplicates(object : IStatusCallback.Stub() {
                        override fun onResult(statusCode: Int, message: String?) {
                            if (cont.isActive) {
                                cont.resume(DedupStatus.fromCode(statusCode))
                                binder?.unlinkToDeath(deathRecipient, 0)
                            }
                            cleanup(connection)
                        }
                    })
                } catch (e: RemoteException) {
                    if (cont.isActive) cont.resume(DedupStatus.ERROR)
                    cleanup(this)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                if (cont.isActive) cont.resume(DedupStatus.ERROR)
                cleanup(this)
            }
        }

        val intent = Intent(context, ContactDedupService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        cont.invokeOnCancellation { cleanup(connection) }
    }

    private fun cleanup(connection: ServiceConnection?) {
        try {
            connection?.let { context.unbindService(it) }
        } catch (e: IllegalArgumentException) {
        }
    }
}