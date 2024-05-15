package capps.lecturetime.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import capps.lecturetime.model.Lecture

@Database(entities = [Lecture::class], version = 1)
@TypeConverters(Converters::class)
abstract class LectureDatabase : RoomDatabase() {
    abstract fun lectureDao(): LectureDao
}