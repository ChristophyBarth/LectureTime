package capps.lecturetime.room

import capps.lecturetime.model.Lecture

interface LectureRepository {
    suspend fun insert(lecture: Lecture): Long

    suspend fun update(lecture: Lecture): Int

    suspend fun getLecture(roomId: Long): Lecture?

    suspend fun delete(lecture: Lecture): Int

    fun getAllLectures(): List<Lecture>

    suspend fun deleteAll(): Int
}