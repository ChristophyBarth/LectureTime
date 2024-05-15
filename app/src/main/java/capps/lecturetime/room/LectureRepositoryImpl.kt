package capps.lecturetime.room

import capps.lecturetime.model.NewLecture
import javax.inject.Inject

class LectureRepositoryImpl @Inject constructor(private val lectureDao: NewLectureDao) : LectureRepository {

    override suspend fun insert(lecture: NewLecture): Long {
        return lectureDao.insertLecture(lecture)
    }

    override suspend fun update(lecture: NewLecture): Int {
        return lectureDao.updateLecture(lecture)
    }

    override suspend fun getLecture(roomId: Long): NewLecture? {
        return lectureDao.getLectureById(roomId)
    }

    override suspend fun deleteAll(): Int {
        return lectureDao.deleteAll()
    }

    override suspend fun delete(lecture: NewLecture): Int {
        return lectureDao.deleteLecture(lecture)
    }

    override fun getAllLectures(): List<NewLecture> {
        return lectureDao.getAllLectures()
    }
}
