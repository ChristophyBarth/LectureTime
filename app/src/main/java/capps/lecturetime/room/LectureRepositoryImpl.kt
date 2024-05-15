package capps.lecturetime.room

import capps.lecturetime.model.Lecture
import javax.inject.Inject

class LectureRepositoryImpl @Inject constructor(private val lectureDao: LectureDao) : LectureRepository {

    override suspend fun insert(lecture: Lecture): Long {
        return lectureDao.insertLecture(lecture)
    }

    override suspend fun update(lecture: Lecture): Int {
        return lectureDao.updateLecture(lecture)
    }

    override suspend fun getLecture(roomId: Long): Lecture? {
        return lectureDao.getLectureById(roomId)
    }

    override suspend fun deleteAll(): Int {
        return lectureDao.deleteAll()
    }

    override suspend fun delete(lecture: Lecture): Int {
        return lectureDao.deleteLecture(lecture)
    }

    override fun getAllLectures(): List<Lecture> {
        return lectureDao.getAllLectures()
    }
}
