package com.mazeppa.firebasechat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mazeppa.firebasechat.adapter.UserAdapter
import com.mazeppa.firebasechat.databinding.ActivityMainBinding
import com.mazeppa.firebasechat.model.User

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }

    private val database by lazy { FirebaseDatabase.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val users = arrayListOf<User>()
    private var user: User? = null
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()
        getCurrentUser()
        getUsers()

        binding.setListeners()
    }

    private fun getCurrentUser() {
        database.reference
            .child("users")
            .child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    private fun getUsers() {
        database.reference
            .child("users")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    users.clear()

                    for (snap in snapshot.children) {
                        val user = snap.getValue(User::class.java)

                        user?.let {
                            //Current user should not be added to the list
                            if (!user.uid.equals(auth.uid)) {
                                users.add(user)
                            }
                        }
                        userAdapter.notifyDataSetChanged()
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setUpRecyclerView() {
        userAdapter = UserAdapter(users)
        val gridLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.recyclerViewUsers.apply {
            layoutManager = gridLayoutManager
            adapter = userAdapter
        }
    }

    private fun ActivityMainBinding.setListeners() {

    }

    override fun onResume() {
        super.onResume()
        val currentUid = auth.uid!!
        database.reference
            .child("presence")
            .child(currentUid)
            .setValue("online")
    }

    override fun onPause() {
        super.onPause()
        val currentUid = auth.uid!!
        database.reference
            .child("presence")
            .child(currentUid)
            .setValue("offline")
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}