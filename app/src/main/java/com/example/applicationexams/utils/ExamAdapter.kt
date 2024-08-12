package com.example.applicationexams.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationexams.databinding.EachExamBinding

class ExamAdapter(private val list:MutableList<ExamData>) :RecyclerView.Adapter<ExamAdapter.ExamViewHolder>(){

    private var listener: ExamAdapterClicksInterface?=null
    fun setListener(listener:ExamAdapterClicksInterface){
        this.listener=listener
    }

    inner class ExamViewHolder(val binding: EachExamBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding=EachExamBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ExamViewHolder(binding)
        }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                binding.examExam.text=this.exam
                binding.deleteExam.setOnClickListener {
                    listener?.onDeleteExamBtnClicked(this)
                }

                binding.editExam.setOnClickListener {
                    listener?.onEditExamBtnClicked(this)
                }
            }
        }
    }

    interface ExamAdapterClicksInterface{
        fun onDeleteExamBtnClicked(examData: ExamData)
        fun onEditExamBtnClicked(examData: ExamData)
    }
}