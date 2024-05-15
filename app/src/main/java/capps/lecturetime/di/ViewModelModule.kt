package capps.lecturetime.di

import android.content.Context
import androidx.room.Room
import capps.lecturetime.room.LectureDatabase
import capps.lecturetime.room.LectureRepository
import capps.lecturetime.room.LectureRepositoryImpl
import capps.lecturetime.room.LectureDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    @Singleton
    fun provideDao(lectureDatabase: LectureDatabase): LectureDao {
        return lectureDatabase.lectureDao()
    }

    @Provides
    @Singleton
    fun provideLectureRepo(lectureRepositoryImpl: LectureRepositoryImpl): LectureRepository {
        return lectureRepositoryImpl
    }

    @Provides
    @Singleton
    fun providesLectureDatabase(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(appContext, LectureDatabase::class.java, "lecture_database").build()
}
