package capps.lecturetime.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import capps.lecturetime.R
import capps.lecturetime.databinding.LectureItemBinding
import capps.lecturetime.fragments.ManageLectureFragmentDirections
import capps.lecturetime.model.Lecture
import capps.lecturetime.utils.Utils.formatTime
import capps.lecturetime.utils.Utils.getDays
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.util.*

class LectureAdapter(
    private val lectures: List<Lecture>,
    private val context: Context,
    private val listener: Listener?,
    private val isManageLectureFragment: Boolean
) : RecyclerView.Adapter<LectureAdapter.LectureAdapterViewHolder>() {

    interface Listener {
        fun deleteLecture(lecture: Lecture)
    }

    inner class LectureAdapterViewHolder(private val binding: LectureItemBinding) : ViewHolder(binding.root) {
        fun bind(lecture: Lecture) {
            binding.courseCode.text = lecture.courseCode
            binding.courseTitle.text = lecture.courseTitle

            val startTimeHr = formatTime(lecture.startTime.first)
            val startTimeMn = formatTime(lecture.startTime.second)
            val endTimeHr = formatTime(lecture.endTime.first)
            val endTimeMn = formatTime(lecture.endTime.second)

            binding.courseTime.text =
                context.getString(R.string.course_startTime_to_endTime, startTimeHr, startTimeMn, endTimeHr, endTimeMn)

            //Check if the lecture is ongoing.
            val startTime = Calendar.getInstance()
            startTime.set(Calendar.HOUR_OF_DAY, lecture.startTime.first)
            startTime.set(Calendar.MINUTE, lecture.startTime.second)

            val endTime = Calendar.getInstance()
            endTime.set(Calendar.HOUR_OF_DAY, lecture.endTime.first)
            endTime.set(Calendar.MINUTE, lecture.endTime.second)

            if (System.currentTimeMillis() in startTime.timeInMillis..endTime.timeInMillis) {
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.my_light_primary))

                binding.apply {
                    courseCode.setTextColor(Color.WHITE)
                    courseTitle.setTextColor(Color.WHITE)
                    courseTime.setTextColor(Color.WHITE)
                    courseDays.setTextColor(Color.WHITE)
                }
            }
            //

            if (lecture.repeatable) {
                binding.repetition.visibility = View.VISIBLE
                binding.courseDays.text = getDays(lecture.days)
            } else {
                binding.repetition.visibility = View.GONE
            }

            if (isManageLectureFragment) {
                binding.courseDays.text = getDays(lecture.days)

                itemView.setOnClickListener {
                    val gson = Gson()
                    val lectureJsonString = gson.toJson(lecture)

                    val action = ManageLectureFragmentDirections.actionManageLectureFragmentToAddLectureFragment(
                        lectureJsonString
                    )
                    it.findNavController().navigate(action)
                }

                itemView.setOnLongClickListener {
                    MaterialAlertDialogBuilder(
                        context, R.style.AlertDialogTheme
                    ).apply {
                        val title = context.getString(R.string.delete_lecture)
                        val spannableTitle = SpannableString(title)
                        spannableTitle.setSpan(
                            StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        setTitle(spannableTitle)
                        setMessage(context.getString(R.string.are_you_sure_you_want_to_delete_this_lecture_you_cannot_recover_it_later))
                        setPositiveButton(context.getString(R.string.no), null)
                        setNegativeButton(
                            context.getString(R.string.yes)
                        ) { _, _ ->
                            listener?.deleteLecture(lecture)
                        }

                        create()
                        show()
                    }

                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureAdapterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return LectureAdapterViewHolder(LectureItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return lectures.size
    }

    override fun onBindViewHolder(holder: LectureAdapterViewHolder, position: Int) {
        holder.bind(lectures[position])
    }
}