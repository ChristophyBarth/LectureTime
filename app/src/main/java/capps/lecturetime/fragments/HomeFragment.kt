package capps.lecturetime.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import capps.lecturetime.AddLectureViewModel
import capps.lecturetime.R
import capps.lecturetime.adapters.LectureAdapter
import capps.lecturetime.databinding.FragmentHomeBinding
import capps.lecturetime.utils.Utils
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LectureAdapter

    private val viewModel: AddLectureViewModel by activityViewModels()

    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getLectures()

        viewModel.lectureList.observe(viewLifecycleOwner) { lectureList ->
            val filteredList = lectureList.filter {
                val now = Calendar.getInstance()

                if (!it.completed) {
                    it.days.contains(now.get(Calendar.DAY_OF_WEEK))
                } else {
                    val completedDate = Calendar.getInstance()
                    completedDate.timeInMillis = it.completedTime

                    completedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) && completedDate.get(Calendar.MONTH) == now.get(
                        Calendar.MONTH
                    ) && completedDate.get(Calendar.DAY_OF_WEEK) == now.get(Calendar.DAY_OF_WEEK)
                }
            }

            val sortedList = filteredList.sortedWith(compareBy({ it.startTime.first }, { it.startTime.second }))

            adapter = LectureAdapter(sortedList, requireContext(), null, false)

            binding.recyclerView.apply {
                adapter = this@HomeFragment.adapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            binding.lectureCount.text = if (sortedList.size == 1) {
                getString(R.string.you_have_1_lecture_today)
            } else {
                getString(R.string.you_have_lectures_today, sortedList.size)
            }
        }

        binding.date.text = Utils.dateFormatter(Date(System.currentTimeMillis()))

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addLectureFragment)
        }

        binding.listButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_manageLectureFragment)
        }

        binding.reload.setOnClickListener {
            Toast.makeText(requireContext(), "Reloading...", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "onViewCreated: Reloading...")
            viewModel.getLectures()
        }
    }
}