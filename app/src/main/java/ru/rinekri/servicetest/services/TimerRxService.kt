package ru.rinekri.servicetest.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.rinekri.servicetest.utils.showToast
import java.util.concurrent.TimeUnit

class TimerRxService : Service() {
  companion object {
    private const val TAG = "TimerRxService"
    private const val TOP_PERIOD = 2L
    private const val EXTRA_START_HANDLER_SERVICE = "$TAG.start_handler_service"

    fun newIntent(context: Context, startHandlerService: Boolean = false): Intent {
      return Intent(context, TimerRxService::class.java).apply {
        putExtra(EXTRA_START_HANDLER_SERVICE, startHandlerService)
      }
    }
  }

  private var compositeDisposable = CompositeDisposable()

  override fun onCreate() {
    Log.e(TAG, "onCreate")
    "$TAG: onCreate".showToast(applicationContext)
  }

  //NOTE: Нужно очищать ресурсы: потоки, ресурсы и т.д.
  override fun onDestroy() {
    compositeDisposable.clear()
    Log.e(TAG, "onDestroy")
    "$TAG: onDestroy".showToast(applicationContext)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e(TAG, "onStartCommand")

    Observable.interval(TOP_PERIOD, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { period ->
        val seconds = period * TOP_PERIOD
        val msg = if (seconds == 0L) {
          "$TAG $startId: invoked"
        } else {
          "$TAG $startId: $seconds seconds elapsed"
        }
        msg.showToast(applicationContext)
        Log.e(TAG, msg)
        if (seconds == 10L && intent?.extras?.getBoolean(EXTRA_START_HANDLER_SERVICE) == true) {
          startService(TimerHandlerService.newIntent(applicationContext, true))
        }
      }
      .also { compositeDisposable.add(it) }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?) = null
}