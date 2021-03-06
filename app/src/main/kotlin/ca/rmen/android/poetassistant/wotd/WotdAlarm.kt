/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.wotd

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import ca.rmen.android.poetassistant.Constants

/**
 * Word of the day task for API levels KitKat and lower.
 * This uses AlarmManager.
 */
object WotdAlarm {
    private val TAG = Constants.TAG + WotdAlarm::class.java.simpleName
    private const val ACTION_WOTD = "action_wotd"

    fun schedule(context : Context) {
        Log.d(TAG, "schedule $context")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),
                Wotd.NOTIFICATION_FREQUENCY_MS,
                getAlarmPendingIntent(context))
    }

    fun cancel(context : Context) {
        Log.d(TAG, "cancel $context")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.cancel(getAlarmPendingIntent(context))
    }

    private fun getAlarmPendingIntent(context : Context) : PendingIntent {
        val intent = Intent(ACTION_WOTD)
        return PendingIntent.getBroadcast(context, TAG.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
