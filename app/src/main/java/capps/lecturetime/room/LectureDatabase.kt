package capps.lecturetime.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import capps.lecturetime.model.NewLecture

@Database(entities = [NewLecture::class], version = 1)
@TypeConverters(Converters::class)
abstract class LectureDatabase : RoomDatabase() {
    abstract fun newLectureDao(): NewLectureDao
}