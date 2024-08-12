package com.example.applicationexams.fragments

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applicationexams.R
//import com.example.applicationexams.R
import com.example.applicationexams.databinding.FragmentHomeBinding
import com.example.applicationexams.utils.ExamAdapter
import com.example.applicationexams.utils.ExamData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment(), AddExamPopUpFragment.DialogNextBtnClickListener,
    ExamAdapter.ExamAdapterClicksInterface {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popUpFragment: AddExamPopUpFragment?=null
    private lateinit var adapter: ExamAdapter
    private lateinit var mList:MutableList<ExamData>
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    private lateinit var manager: NotificationManager
    val NOTIFICATION_ID = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addBtnHome.setOnClickListener {
            registerEvents()
        }



        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.logout -> {
                        auth.signOut()
                        navController.navigate(R.id.action_homeFragment_to_signInFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        init(view)
        getDataFromFirebase()

        loadData()
        binding.saveButton.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {
        val insertedText : String = binding.etText.text.toString()
        binding.tvText.text = insertedText
        val sharedPreferences : SharedPreferences =
            requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.apply{
            putString("STRING_KEY", insertedText)
        }.apply()
        Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
        binding.etText.setText("")

    }

    private fun loadData() {
        val sharedPreferences : SharedPreferences =
            requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedString : String? = sharedPreferences.getString("STRING_KEY",
            null)
        binding.tvText.text = savedString
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for(taskSnapshot in snapshot.children){
                    val examExam= taskSnapshot.key?.let {
                        ExamData(it, taskSnapshot.value.toString())
                    }
                    if(examExam != null){
                        mList.add(examExam)
                    }
                }
                adapter.notifyDataSetChanged()

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun registerEvents() {
        if(popUpFragment!=null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()

       popUpFragment= AddExamPopUpFragment()
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager,  AddExamPopUpFragment.TAG)
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Exams").child(auth.currentUser?.uid.toString())

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = ExamAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter

        createNotificationChannel()

    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                    lightColor = Color.RED
                    enableLights(true)
                }
            manager =
                requireContext().getSystemService<NotificationManager>()!!
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onSaveExam(exam: String, examEt: TextInputEditText) {
        databaseRef.push().setValue(exam).addOnCompleteListener {
            if(it.isSuccessful){
                val notification =
                    NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setContentTitle("Exam notification")
                        .setContentText("Exam: '${exam}' Was Successfully Added!")
                        .setSmallIcon(R.drawable.baseline_android_24)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                val notificationManager =
                    NotificationManagerCompat.from(requireContext())
                notificationManager.notify(NOTIFICATION_ID, notification)


            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            popUpFragment!!.dismiss()
        }
    }

    override fun onUpdateExam(examData: ExamData, examEt: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[examData.examId] = examData.exam
        databaseRef.updateChildren(map).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Exam Updated successfully!",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message,
                    Toast.LENGTH_SHORT).show()
            }
            examEt.text = null
            popUpFragment!!.dismiss()
        }
    }


    override fun onDeleteExamBtnClicked(examData: ExamData) {
        databaseRef.child(examData.examId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Exam Deleted Successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditExamBtnClicked(examData: ExamData) {
        if(popUpFragment!=null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()

        popUpFragment= AddExamPopUpFragment.newInstance(examData.examId,examData.exam)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager, AddExamPopUpFragment.TAG)
    }
    
}