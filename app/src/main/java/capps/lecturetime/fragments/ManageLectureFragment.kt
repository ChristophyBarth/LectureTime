package capps.lecturetime.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Operation
import androidx.work.WorkManager
import capps.lecturetime.AddLectureViewModel
import capps.lecturetime.R
import capps.lecturetime.adapters.LectureAdapter
import capps.lecturetime.databinding.FragmentManageLectureBinding
import capps.lecturetime.model.Lecture
import capps.lecturetime.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ManageLectureFragment : Fragment() {
    private var _binding: FragmentManageLectureBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LectureAdapter

    private val viewModel: AddLectureViewModel by activityViewModels()

    companion object {
        const val TAG = "ManageLectureFragment"
    }

    private var loadingMaterialAlertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageLectureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.getLectures()
        viewModel.lectureList.observe(viewLifecycleOwner) { lectureList ->

            val sortedList = lectureList.sortedWith(compareBy({ it.days.minOrNull() },
                { it.startTime.first },
                { it.startTime.second })
            )

            adapter = LectureAdapter(sortedList, requireContext(), object : LectureAdapter.Listener {
                override fun deleteLecture(lecture: Lecture) {
                    for (uuid in lecture.uIds!!) {
                        WorkManager.getInstance(requireContext()).cancelWorkById(uuid)
                    }

                    viewModel.deletingSpecificLecture(lecture)
                }

            }, true)

            binding.allLecturesRecyclerView.apply {
                adapter = this@ManageLectureFragment.adapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            if (lectureList.isEmpty()) {
                binding.lottie.visibility = View.VISIBLE
            } else {
                binding.allLecturesRecyclerView.visibility = View.VISIBLE
                binding.lottie.visibility = View.INVISIBLE
            }
        }


        viewModel.deletingSpecificLectureResource.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resource ->
                when (resource) {
                    is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    is Resource.Loading -> {
                        Log.d(TAG, "onViewCreated: ${resource.data}")
                    }

                    is Resource.Success -> {
                        Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        viewModel.deletingResource.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resource ->
                when (resource) {
                    is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    is Resource.Loading -> {
                        showLoadingDialog(resource.data)
                    }

                    is Resource.Success -> {
                        Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                        loadingMaterialAlertDialog?.dismiss()
                        loadingMaterialAlertDialog = null
                    }
                }
            }
        }

        binding.deleteAll.setOnClickListener {
            MaterialAlertDialogBuilder(
                requireContext(), R.style.AlertDialogTheme
            ).apply {
                val title = "Delete Everything?"
                val spannableTitle = SpannableString(title)
                spannableTitle.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                setTitle(spannableTitle)
                setMessage("Are you sure you want to delete all lectures?\nDeleted lectures cannot be recovered!")
                setPositiveButton("CANCEL", null)
                setNegativeButton(
                    "DELETE"
                ) { _, _ ->
                    deleteAllLectures()
                }

                create()
                show()
            }
        }
    }

    private fun showLoadingDialog(message: String?) {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext()).apply {
            setView(R.layout.dialog_progress)
            setMessage(message)
            setCancelable(false)
        }

        loadingMaterialAlertDialog = materialAlertDialogBuilder.create()
        loadingMaterialAlertDialog?.show()
    }

    @SuppressLint("RestrictedApi")
    private fun deleteAllLectures() {
        if (viewModel.lectureList.value!!.isNotEmpty()) {
            val workManager = WorkManager.getInstance(requireContext())

            val cancelAllWork = workManager.cancelAllWork()
            cancelAllWork.state.observe(viewLifecycleOwner) { operation ->
                if (operation == Operation.SUCCESS) {
                    viewModel.deleteAllLectures()
                } else {
                    Toast.makeText(requireContext(), "Error deleting lectures", Toast.LENGTH_LONG).show()
                }

                Log.i(TAG, "deleteAllLectures: $operation")
            }

        } else {
            Toast.makeText(requireContext(), "There are no lectures to delete.", Toast.LENGTH_LONG).show()
        }
    }
}