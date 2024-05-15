package capps.lecturetime.room

import androidx.room.*
import capps.lecturetime.model.Lecture

@Dao
interface LectureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: Lecture): Long

    @Update
    suspend fun updateLecture(lecture: Lecture): Int

    @Query("SELECT * FROM lectures WHERE id = :roomId")
    suspend fun getLectureById(roomId: Long): Lecture?

    @Delete
    suspend fun deleteLecture(lecture: Lecture): Int

    @Query("SELECT * FROM lectures")
    fun getAllLectures(): List<Lecture>

    @Query("DELETE FROM lectures")
    suspend fun deleteAll(): Int
}