package com.mazeppa.firebasechat

import android.annotation.SuppressLint
import android.content.Intent
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
import com.mazeppa.firebasechat.util.DatabaseChild
import com.mazeppa.firebasechat.util.IntentExtras
import com.mazeppa.firebasechat.util.Presence

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }

    private val database by lazy { FirebaseDatabase.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val currentUid by lazy { auth.uid!! }
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
        setPresence(Presence.Online)
        binding.setListeners()
    }

    private fun getCurrentUser() {
        database.reference
            .child(DatabaseChild.USERS)
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
            .child(DatabaseChild.USERS)
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
                    }
                    userAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setUpRecyclerView() {
        userAdapter = UserAdapter(users) { user ->
            startActivity(Intent(this@MainActivity, ChatActivity::class.java).apply {
                putExtra(IntentExtras.USER, user)
            })
        }
        val gridLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.recyclerViewUsers.apply {
            layoutManager = gridLayoutManager
            adapter = userAdapter
        }
    }

    private fun ActivityMainBinding.setListeners() {

    }

    private fun setPresence(presence: Presence) {
        database.reference
            .child(DatabaseChild.PRESENCE)
            .child(currentUid)
            .setValue(presence.status)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        setPresence(Presence.Offline)
    }
}