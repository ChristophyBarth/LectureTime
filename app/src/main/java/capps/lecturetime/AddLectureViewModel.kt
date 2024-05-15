package capps.lecturetime

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import capps.lecturetime.utils.Utils.dateFormatter
import capps.lecturetime.utils.Utils.millisUntilNextTime
import capps.lecturetime.model.Lecture
import capps.lecturetime.room.LectureRepository
import capps.lecturetime.utils.Event
import capps.lecturetime.utils.Resource
import capps.lecturetime.workmanager.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class AddLectureViewModel @Inject constructor(private val lectureRepository: LectureRepository) : ViewModel() {
    companion object {
        const val TAG = "AddLectureViewModel"
    }

    private val _lectureList = MutableLiveData<List<Lecture>>(mutableListOf())
    val lectureList: LiveData<List<Lecture>> get() = _lectureList

    private val _loadingResource = MutableLiveData<Event<Resource<String>>>()
    val loadingResource: LiveData<Event<Resource<String>>> get() = _loadingResource

    private val _deletingSpecificLectureResource = MutableLiveData<Event<Resource<String>>>()
    val deletingSpecificLectureResource: LiveData<Event<Resource<String>>> get() = _deletingSpecificLectureResource

    private val _deletingResource = MutableLiveData<Event<Resource<String>>>()
    val deletingResource: LiveData<Event<Resource<String>>> get() = _deletingResource

    private val _periodicWorkRequests =
        MutableLiveData<Event<MutableList<Pair<String, PeriodicWorkRequest>>>>(Event(mutableListOf()))
    val periodicWorkRequests: LiveData<Event<MutableList<Pair<String, PeriodicWorkRequest>>>> get() = _periodicWorkRequests

    private val _cancelPreviousWork = MutableLiveData<Event<List<UUID>?>>(Event(null))
    val cancelPreviousWork: LiveData<Event<List<UUID>?>> get() = _cancelPreviousWork

    private val _oneTimeWorkRequests = MutableLiveData<Event<MutableList<Pair<String, OneTimeWorkRequest>>>>(
        Event(
            mutableListOf()
        )
    )
    val oneTimeWorkRequests: LiveData<Event<MutableList<Pair<String, OneTimeWorkRequest>>>> get() = _oneTimeWorkRequests

    private val lecture = MutableLiveData<Lecture?>()

    fun getLectures() {
        viewModelScope.launch(Dispatchers.IO) {
            val listOfLectures = lectureRepository.getAllLectures()
            _lectureList.postValue(listOfLectures)
        }
    }

    fun scheduleLecture(
        id: Long?,
        uuids: List<UUID>?,
        courseCode: String,
        courseTitle: String,
        startTime: Pair<Int, Int>,
        endTime: Pair<Int, Int>,
        days: List<Int>,
        repeatable: Boolean
    ) {
        _loadingResource.value = Event(Resource.Loading("LOADING..."))

        val uniqueWorkID = id ?: abs(UUID.randomUUID().hashCode().toLong())
        if (id != null) _cancelPreviousWork.value = Event(uuids)

        val lecture = Lecture(
            id = uniqueWorkID,
            courseCode = courseCode,
            courseTitle = courseTitle,
            startTime = startTime,
            endTime = endTime,
            days = days,
            repeatable = repeatable,
            completed = false
        )

        val constraints =
            Constraints.Builder().setRequiresCharging(false).setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false).build()

        val uIds = mutableListOf<UUID>()
        if (lecture.repeatable) {
            val requests = mutableListOf<Pair<String, PeriodicWorkRequest>>()
            for (day in days) {
                val initialDelay = millisUntilNextTime(day, startTime.first, startTime.second)

                //TODO("Remove later it's just a log to see the starting dates for each selected day")
                val calendar = Calendar.getInstance()
                val scheduledDay = dateFormatter(Date(calendar.timeInMillis + initialDelay))
                Log.i(TAG, "scheduleLecture: $scheduledDay")
                //TODO("Remove later it's just a log to see the starting dates for each selected day")

                val uid = UUID.randomUUID()
                uIds.add(uid)

                val inputData = Data.Builder().putString("title", lecture.courseCode)
                    .putString("message", "You have ${lecture.courseTitle} lecture now")
                    .putLong("roomId", uniqueWorkID).putBoolean("repeatable", repeatable).build()

                val notificationRequest =
                    PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS).setConstraints(constraints)
                        .setId(uid).setInputData(inputData).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS).build()

                requests.add(
                    Pair(
                        uid.toString(), notificationRequest
                    )
                )
            }

            lecture.uIds = uIds
            this.lecture.value = lecture

            _periodicWorkRequests.value = Event(requests)
        } else {
            val requests = mutableListOf<Pair<String, OneTimeWorkRequest>>()
            for (day in days) {
                val initialDelay = millisUntilNextTime(day, startTime.first, startTime.second)

                val uid = UUID.randomUUID()
                uIds.add(uid)

                val inputData = Data.Builder().putString("title", lecture.courseCode)
                    .putString("message", "You have ${lecture.courseTitle} lecture now")
                    .putLong("roomId", uniqueWorkID).putBoolean("repeatable", repeatable).build()

                val notificationRequest =
                    OneTimeWorkRequestBuilder<NotificationWorker>().setConstraints(constraints).setId(uid)
                        .setInputData(inputData).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS).build()

                requests.add(
                    Pair(
                        uid.toString(), notificationRequest
                    )
                )
            }

            lecture.uIds = uIds
            this.lecture.value = lecture

            _oneTimeWorkRequests.value = Event(requests)
        }
    }

    fun saveLecture() {
        viewModelScope.launch(Dispatchers.IO) {
            val value = lectureRepository.insert(lecture.value!!)

            if (value > 0) {
                lecture.postValue(null)
                _loadingResource.postValue(Event(Resource.Success("Lecture added successfully!")))
            } else {
                _loadingResource.postValue(Event(Resource.Error("Error: Couldn't add this lecture")))
            }
        }
    }

    fun deletingSpecificLecture(lecture: Lecture) {
        _deletingSpecificLectureResource.postValue(Event(Resource.Loading("Deleting...")))

        viewModelScope.launch(Dispatchers.IO) {
            val delete = lectureRepository.delete(lecture)

            if (delete > 0) {
                getLectures()
                _deletingSpecificLectureResource.postValue(Event(Resource.Success("Lecture delete successfully!")))
            } else {
                _deletingSpecificLectureResource.postValue(Event(Resource.Error("Couldn't delete that lecture")))
            }
        }
    }

    fun deleteAllLectures() {
        _deletingResource.postValue(Event(Resource.Loading("Deleting lectures...")))

        viewModelScope.launch(Dispatchers.IO) {
            val delete = lectureRepository.deleteAll()

            if (delete > 0) {
                delay(3000)
                _deletingResource.postValue(Event(Resource.Success("All lectures successfully deleted!")))
                _lectureList.postValue(mutableListOf())
            } else {
                _deletingResource.postValue(Event(Resource.Error("Error deleting lectures")))
            }
        }
    }
}