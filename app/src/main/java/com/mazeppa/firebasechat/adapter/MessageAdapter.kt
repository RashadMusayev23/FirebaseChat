package com.mazeppa.firebasechat.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mazeppa.firebasechat.R
import com.mazeppa.firebasechat.databinding.ItemReceivedMessageBinding
import com.mazeppa.firebasechat.databinding.ItemSentMessageBinding
import com.mazeppa.firebasechat.databinding.LayoutDeleteMessageBinding
import com.mazeppa.firebasechat.model.Message
import com.mazeppa.firebasechat.util.DatabaseChild
import com.mazeppa.firebasechat.util.MessageType

/**
 * Rashad Musayev on 5/1/2023 - 17:31
 */
class MessageAdapter(
    private val messages: List<Message>,
    private val senderRoom: String,
    private val receiverRoom: String,
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2

    class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSentMessageBinding.bind(itemView)
    }

    class ReceiveMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemReceivedMessageBinding.bind(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is SentMessageHolder) {
            if (message.message.equals(MessageType.PHOTO)) {
                holder.binding.apply {
                    constraintLayoutPhotoBox.visibility = View.VISIBLE
                    constraintLayoutTextBox.visibility = View.GONE

                    imageViewPhotoMessage.load(message.imageUrl)
                    textViewPhotoTime.text = message.time
                }
            } else {
                holder.binding.textViewMessageTime.text = message.time
                holder.binding.textViewMessage.text = message.message
            }

            setupDeleteMessageDialog(
                binding = holder.binding,
                deleteForEveryone = {
                    message.message = "This message is removed"
                    message.messageId?.let { id ->
                        FirebaseDatabase.getInstance().reference
                            .child(DatabaseChild.CHATS)
                            .child(senderRoom)
                            .child("message")
                            .child(id).setValue(message)
                    }
                    message.messageId?.let { id ->
                        FirebaseDatabase.getInstance().reference
                            .child(DatabaseChild.CHATS)
                            .child(receiverRoom)
                            .child("message")
                            .child(id).setValue(message)
                    }
                },
                deleteForMe = {
                    message.messageId?.let { id ->
                        FirebaseDatabase.getInstance().reference
                            .child(DatabaseChild.CHATS)
                            .child(senderRoom)
                            .child("message")
                            .child(id).setValue(null)
                    }
                }
            )
        } else if (holder is ReceiveMessageHolder) {
            if (message.message.equals(MessageType.PHOTO)) {
                holder.binding.apply {
                    constraintLayoutPhotoBox.visibility = View.VISIBLE
                    constraintLayoutTextBox.visibility = View.GONE

                    imageViewPhotoMessage.load(message.imageUrl)
                    textViewPhotoTime.text = message.time
                }
            } else {
                holder.binding.textViewMessageTime.text = message.time
                holder.binding.textViewMessage.text = message.message
            }
        }
    }

    /**
     * Creates Alert Dialog when [binding]'s root is long clicked and gives 2 options to delete
     * message
     */
    private fun setupDeleteMessageDialog(
        binding: ViewBinding,
        deleteForEveryone: () -> Unit,
        deleteForMe: () -> Unit,
    ) {
        binding.root.apply {
            setOnLongClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_delete_message, null)
                val deleteLayoutBinding = LayoutDeleteMessageBinding.bind(view)

                val dialog = AlertDialog.Builder(context).apply {
                    setTitle(R.string.delete_message)
                    setView(deleteLayoutBinding.root)
                }.create()

                deleteLayoutBinding.textViewDeleteForEveryone.setOnClickListener {
                    deleteForEveryone()
                    dialog.dismiss()
                }
                deleteLayoutBinding.textViewDeleteForMe.setOnClickListener {
                    deleteForMe()
                    dialog.dismiss()
                }
                deleteLayoutBinding.textViewCancel.setOnClickListener { dialog.dismiss() }

                dialog.show()

                false
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val auth = FirebaseAuth.getInstance()

        println("Current UID: ${auth.uid}")
        println("Sender UID: ${message.senderId}")

        //If sender of message is current user
        return if (auth.uid.equals(message.senderId)) {
            println("Item Sent")
            ITEM_SENT
        } else {
            println("Item Received")
            ITEM_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == ITEM_SENT) {
            SentMessageHolder(
                layoutInflater.inflate(R.layout.item_sent_message, parent, false)
            )
        } else {
            ReceiveMessageHolder(
                layoutInflater.inflate(R.layout.item_received_message, parent, false)
            )
        }
    }

    override fun getItemCount() = messages.size

}