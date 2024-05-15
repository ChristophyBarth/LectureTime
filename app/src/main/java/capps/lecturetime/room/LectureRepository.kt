package capps.lecturetime.room

import capps.lecturetime.model.NewLecture

interface LectureRepository {
    suspend fun insert(lecture: NewLecture): Long

    suspend fun update(lecture: NewLecture): Int

    suspend fun getLecture(roomId: Long): NewLecture?

    suspend fun delete(lecture: NewLecture): Int

    fun getAllLectures(): List<NewLecture>

    suspend fun deleteAll(): Int
}