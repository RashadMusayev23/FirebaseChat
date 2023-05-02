package com.mazeppa.firebasechat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mazeppa.firebasechat.adapter.MessageAdapter
import com.mazeppa.firebasechat.databinding.ActivityChatBinding
import com.mazeppa.firebasechat.model.Message
import com.mazeppa.firebasechat.model.User
import com.mazeppa.firebasechat.util.DatabaseChild
import com.mazeppa.firebasechat.util.IntentExtras
import com.mazeppa.firebasechat.util.dateFormat
import okhttp3.internal.format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private var _binding: ActivityChatBinding? = null
    private val binding: ActivityChatBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var messageAdapter: MessageAdapter? = null
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private val messages = arrayListOf<Message>()
    private val user by lazy { intent.getSerializableExtra(IntentExtras.USER) as User }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        binding.bindViews(user)
        binding.setListeners()
        setupRecyclerView()
        getMessages()
    }

    private fun init() {
        receiverUid = user.uid
        senderUid = auth.uid
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        messageAdapter = MessageAdapter(messages, senderRoom, receiverRoom)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.apply {
            val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
            layoutManager = linearLayoutManager
            adapter = messageAdapter
        }
    }

    private fun ActivityChatBinding.bindViews(user: User) {

        imageViewProfileImage.load(user.profileImage)

        textViewUserName.text = user.name

        //Get Online Status
        database.reference
            .child(DatabaseChild.PRESENCE)
            .child(user.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        textViewOnlineStatus.text = snapshot.value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun ActivityChatBinding.setListeners() {
        buttonSend.setOnClickListener {
            if (editTextMessageBox.text.toString().isNotEmpty()) {
                val formattedDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val messageText = editTextMessageBox.text.toString()
                val message = Message(message = messageText, senderId = senderUid, time = formattedDate)

                val randomKey = database.reference.push().key

                val lastMessageObject = HashMap<String, Any>()
                lastMessageObject["lastMsg"] = message.message!!
                lastMessageObject["lastMsgTime"] = formattedDate

                //Update Last Message
                database.reference.child(DatabaseChild.CHATS)
                    .child(senderRoom)
                    .updateChildren(lastMessageObject)

                //Update Last Message
                database.reference.child(DatabaseChild.CHATS)
                    .child(receiverRoom)
                    .updateChildren(lastMessageObject)

                database.reference.child(DatabaseChild.CHATS)
                    .child(senderRoom)
                    .child(DatabaseChild.MESSAGES)
                    .child(randomKey!!)
                    .setValue(message).addOnSuccessListener {
                        database.reference.child(DatabaseChild.CHATS)
                            .child(receiverRoom)
                            .child(DatabaseChild.MESSAGES)
                            .child(randomKey)
                            .setValue(message)
                            .addOnSuccessListener {

                            }
                    }
            }
            editTextMessageBox.text.clear()
        }

        imageButtonNavigateUp.setOnClickListener {
            finish()
        }

        val handler = Handler(mainLooper)
        editTextMessageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                database.reference.child(DatabaseChild.PRESENCE)
                    .child(senderUid!!)
                    .setValue("typing...")

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database.reference.child(DatabaseChild.PRESENCE)
                    .child(senderUid!!)
                    .setValue("online")
            }

        })
    }

    private fun getMessages() {
        database.reference
            .child(DatabaseChild.CHATS)
            .child(senderRoom)
            .child(DatabaseChild.MESSAGES)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()

                    for (snap in snapshot.children) {
                        val message = snap.getValue(Message::class.java)
                        message?.messageId = snap.key
                        if (message != null) {
                            messages.add(message)
                        }
                    }

                    messageAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
