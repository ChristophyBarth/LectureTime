package capps.lecturetime.room

import androidx.room.*
import capps.lecturetime.model.NewLecture

@Dao
interface NewLectureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: NewLecture): Long

    @Update
    suspend fun updateLecture(lecture: NewLecture): Int

    @Query("SELECT * FROM lectures WHERE id = :roomId")
    suspend fun getLectureById(roomId: Long): NewLecture?

    @Delete
    suspend fun deleteLecture(lecture: NewLecture): Int

    @Query("SELECT * FROM lectures")
    fun getAllLectures(): List<NewLecture>

    @Query("DELETE FROM lectures")
    suspend fun deleteAll(): Int
}