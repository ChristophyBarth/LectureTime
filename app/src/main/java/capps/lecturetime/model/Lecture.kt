package capps.lecturetime.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "lectures")
data class Lecture(
    @PrimaryKey var id: Long = 0,
    @ColumnInfo(name = "uIds") var uIds: List<UUID>? = null,
    @ColumnInfo(name = "courseCode") var courseCode: String,
    @ColumnInfo(name = "courseTitle") var courseTitle: String,
    @ColumnInfo(name = "startTime") var startTime: Pair<Int, Int>,
    @ColumnInfo(name = "endTime") var endTime: Pair<Int, Int>,
    @ColumnInfo(name = "days") var days: List<Int>,
    @ColumnInfo(name = "repeatable") var repeatable: Boolean,
    @ColumnInfo(name = "completed") var completed: Boolean,
    @ColumnInfo(name = "completedTime") var completedTime: Long = 0
)