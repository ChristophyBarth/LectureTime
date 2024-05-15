package capps.lecturetime.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import capps.lecturetime.AddLectureViewModel
import capps.lecturetime.R
import capps.lecturetime.utils.Resource
import capps.lecturetime.databinding.FragmentAddLectureBinding
import capps.lecturetime.model.NewLecture
import com.google.gson.Gson
import java.util.*

class AddLectureFragment : Fragment() {
    private var _binding: FragmentAddLectureBinding? = null
    private val binding get() = _binding!!

    private var startTime: Pair<Int, Int>? = null
    private var endTime: Pair<Int, Int>? = null

    var courseCode: String? = null
    var courseTitle: String? = null

    lateinit var courseCodeData: MutableList<String>
    lateinit var courseTitleData: MutableList<String>

    private val viewModel: AddLectureViewModel by activityViewModels()

    companion object {
        const val TAG = "AddLectureFragment"
    }

    private val args: AddLectureFragmentArgs by navArgs()
    private var id: Long? = null
    private var uuids: List<UUID>? = null

    private lateinit var workManager: WorkManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        workManager = WorkManager.getInstance(requireContext())

        courseCodeData = mutableListOf("COM 111", "GNS 123", "EED 232", "COM 142")
        courseCodeData = courseCodeData.sorted().toMutableList()
        courseCodeData.add(0, "select course code")

        courseTitleData = mutableListOf("COM 111 title", "GNS 123 title", "EED 232 title", "COM 142 title")
        courseTitleData = courseTitleData.sorted().toMutableList()
        courseTitleData.add(0, "select course title")

        val courseCodeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseCodeData)
        courseCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseCodeSpinner.adapter = courseCodeAdapter
        binding.courseCodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.courseTitleSpinner.setSelection(position)
                courseCode = courseCodeData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val courseTitleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseTitleData)
        courseTitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseTitleSpinner.adapter = courseTitleAdapter
        binding.courseTitleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.courseCodeSpinner.setSelection(position)
                courseTitle = courseTitleData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (args.lectureJsonString != "null") {
            Log.i(TAG, "onViewCreated: ${args.lectureJsonString}")

            val lecture = Gson().fromJson(args.lectureJsonString, NewLecture::class.java)
            id = lecture.id
            uuids = lecture.uIds

            startTime = lecture.startTime
            endTime = lecture.endTime
            courseCode = lecture.courseCode
            courseTitle = lecture.courseTitle

            binding.apply {
                toolbar.title = getString(R.string.edit_lecture)
                saveButton.text = getString(R.string.update)

                val titleIndex = courseTitleData.indexOf(lecture.courseTitle)
                courseTitleSpinner.setSelection(titleIndex)
                courseCodeSpinner.setSelection(titleIndex)

                startTime.text =
                    String.format(getString(R.string.time_format), lecture.startTime.first, lecture.startTime.second)
                endTime.text =
                    String.format(getString(R.string.time_format), lecture.endTime.first, lecture.endTime.second)

                sunday.isChecked = lecture.days.contains(1)
                monday.isChecked = lecture.days.contains(2)
                tuesday.isChecked = lecture.days.contains(3)
                wednesday.isChecked = lecture.days.contains(4)
                thursday.isChecked = lecture.days.contains(5)
                friday.isChecked = lecture.days.contains(6)
                saturday.isChecked = lecture.days.contains(7)

                repeatWeeklySwitch.isChecked = lecture.repeatable
            }
        }

        viewModel.apply {
            oneTimeWorkRequests.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { requestList ->
                    for (request in requestList) {
                        workManager.enqueueUniqueWork(
                            request.first, ExistingWorkPolicy.REPLACE, request.second
                        )
                    }

                    if (requestList.isNotEmpty()) {
                        saveLecture()
                    }
                }
            }

            periodicWorkRequests.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { requestList ->
                    for (request in requestList) {
                        workManager.enqueueUniquePeriodicWork(
                            request.first, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request.second
                        )
                    }

                    if (requestList.isNotEmpty()) {
                        saveLecture()
                    }
                }
            }

            cancelPreviousWork.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { uuids ->
                    for (uuid in uuids) {
                        workManager.cancelWorkById(uuid)
                    }
                }
            }

            loadingResource.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            binding.saveButton.isEnabled = true
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Loading -> {
                            binding.saveButton.isEnabled = false
                            Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Success -> {
                            Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }

        binding.startTimeLinearLayout.setOnClickListener {
            showTimeDialog(true)
        }

        binding.endTimeLinearLayout.setOnClickListener {
            showTimeDialog(false)
        }

        binding.apply {
            saveButton.setOnClickListener {
                if (checkIfValid()) {
                    val days = mutableListOf<Int>().apply {
                        if (monday.isChecked) add(Calendar.MONDAY)
                        if (tuesday.isChecked) add(Calendar.TUESDAY)
                        if (wednesday.isChecked) add(Calendar.WEDNESDAY)
                        if (thursday.isChecked) add(Calendar.THURSDAY)
                        if (friday.isChecked) add(Calendar.FRIDAY)
                        if (saturday.isChecked) add(Calendar.SATURDAY)
                        if (sunday.isChecked) add(Calendar.SUNDAY)
                    }

                    viewModel.scheduleLecture(
                        id = this@AddLectureFragment.id,
                        uuids = this@AddLectureFragment.uuids,
                        courseCode = this@AddLectureFragment.courseCode!!,
                        courseTitle = this@AddLectureFragment.courseTitle!!,
                        startTime = this@AddLectureFragment.startTime!!,
                        endTime = this@AddLectureFragment.endTime!!,
                        days = days,
                        repeatable = repeatWeeklySwitch.isChecked,
                    )
                }
            }
        }
    }

    private fun checkIfValid(): Boolean {
        binding.apply {
            if (courseCode == null || courseCode == courseCodeData[0]) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, 0)
                }

                courseCodeSpinner.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(), R.anim.vibrate
                    )
                )

                courseTitleSpinner.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(), R.anim.vibrate
                    )
                )
            } else if (this@AddLectureFragment.startTime == null) {
                Toast.makeText(
                    requireContext(), getString(R.string.lecture_start_time_has_not_been_set), Toast.LENGTH_SHORT
                ).show()
            } else if (this@AddLectureFragment.endTime == null) {
                Toast.makeText(
                    requireContext(), getString(R.string.lecture_end_time_has_not_been_set), Toast.LENGTH_SHORT
                ).show()
            } else if (this@AddLectureFragment.startTime!!.first > this@AddLectureFragment.endTime!!.first) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.lecture_end_time_must_be_later_than_start_time),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (this@AddLectureFragment.startTime!!.first == this@AddLectureFragment.endTime!!.first && this@AddLectureFragment.startTime!!.second > this@AddLectureFragment.endTime!!.second) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.lecture_end_time_must_be_later_than_start_time),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (this@AddLectureFragment.startTime!!.first == this@AddLectureFragment.endTime!!.first && this@AddLectureFragment.startTime!!.second == this@AddLectureFragment.endTime!!.second) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.lecture_start_time_and_end_time_can_t_be_the_same),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!monday.isChecked && !tuesday.isChecked && !wednesday.isChecked && !thursday.isChecked && !friday.isChecked && !saturday.isChecked && !sunday.isChecked) {
                Toast.makeText(
                    requireContext(), getString(R.string.lecture_day_hasn_t_been_selected), Toast.LENGTH_SHORT
                ).show()
            } else {
                return true
            }
        }

        return false
    }

    private fun showTimeDialog(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()

        var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        if (isStartTime && startTime != null) {
            hourOfDay = startTime!!.first
            minute = startTime!!.second
        }

        if (!isStartTime && endTime != null) {
            hourOfDay = endTime!!.first
            minute = endTime!!.second
        }

        val timePickerDialog = TimePickerDialog(
            requireContext(), { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val selectedTime = String.format(getString(R.string.time_format), selectedHour, selectedMinute)

                if (isStartTime) {
                    binding.startTime.text = selectedTime
                    this.startTime = Pair(selectedHour, selectedMinute)
                } else {
                    binding.endTime.text = selectedTime
                    this.endTime = Pair(selectedHour, selectedMinute)
                }
            }, hourOfDay, minute, true
        )

        timePickerDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}