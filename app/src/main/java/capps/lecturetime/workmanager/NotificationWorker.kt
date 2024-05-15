package capps.lecturetime.workmanager

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import capps.lecturetime.R
import capps.lecturetime.room.LectureDatabase

const val TAG = "NotificationWorker"

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val inputData = inputData
        val roomId = inputData.getLong("roomId", 0)
        val title = inputData.getString("title")
        val message = inputData.getString("message")
        val repeatable = inputData.getBoolean("repeatable", false)

        Log.e(TAG, "doWork1: $title and $message")

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createOreoNotification(title, message)
        } else {
            NotificationCompat.Builder(applicationContext, CHANNEL_ID).setSmallIcon(R.drawable.ic_plus)
                .setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        }

        showNotification(notification, roomId)

        if (!repeatable) {
            markAsCompleted(roomId)
        }

        Log.e(TAG, "doWork2: $roomId")
        return Result.success()
    }

    private suspend fun markAsCompleted(roomId: Long) {
        val database = Room.databaseBuilder(
            applicationContext, LectureDatabase::class.java, "lecture_database"
        ).build()

        val dao = database.lectureDao()

        val lecture = dao.getLectureById(roomId)

        if (lecture != null) {
            lecture.completed = true
            lecture.completedTime = System.currentTimeMillis()

            dao.updateLecture(
                lecture
            )

            Log.d(TAG, "markAsCompleted: ${lecture.courseCode}")
        }
    }

    private fun showNotification(notification: Notification, roomId: Long) {
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //permission not granted to show notification
                return
            }

            notify(roomId.toInt(), notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createOreoNotification(title: String?, message: String?): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID, "Lecture Notifications", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "This channel shows lecture notifications."
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


        return Notification.Builder(applicationContext, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title).setContentText(message).build()
    }

    companion object {
        const val CHANNEL_ID = "channel001"
    }
}
