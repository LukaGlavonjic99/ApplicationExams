package com.example.applicationexams.fragments

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.applicationexams.R
import com.example.applicationexams.databinding.FragmentAddExamPopUpBinding
import com.example.applicationexams.utils.ExamData
import com.google.android.material.textfield.TextInputEditText


class AddExamPopUpFragment : DialogFragment() {

    private lateinit var binding: FragmentAddExamPopUpBinding
    private lateinit var listener: DialogNextBtnClickListener
    private var examData : ExamData? = null

    fun setListener(listener: DialogNextBtnClickListener){
        this.listener = listener
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddExamPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            examData = ExamData(arguments?.getString("examId").toString(),
                arguments?.getString("exam").toString())
            binding.examEt.setText(examData?.exam)
        }

        registerEvents()
    }

    private fun registerEvents() {
        binding.examNextBtn.setOnClickListener{
            val examExam = binding.examEt.text.toString().trim()
            if(examExam.isNotEmpty()){
                if(examData==null){
                    listener.onSaveExam(examExam, binding.examEt)
                }
                else{
                    examData?.exam=examExam
                    listener.onUpdateExam(examData!!, binding.examEt)

                }
            } else {
                Toast.makeText(context, "You did not enter exam!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.examClose.setOnClickListener {
            dismiss()
        }

    }

    interface DialogNextBtnClickListener{
        fun onSaveExam(exam: String, examEt: TextInputEditText)
        fun onUpdateExam(examData: ExamData, examEt: TextInputEditText)

    }

    companion object{
        const val TAG = "AddExamPopUpFragment"
        @JvmStatic
        fun newInstance(examId: String, exam : String) =
            AddExamPopUpFragment().apply {
                arguments = Bundle().apply {
                    putString("examId", examId)
                    putString("exam", exam)
                }
            }

    }

}