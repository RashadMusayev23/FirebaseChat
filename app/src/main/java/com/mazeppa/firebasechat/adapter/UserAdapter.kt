package com.mazeppa.firebasechat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mazeppa.firebasechat.databinding.ItemProfileBinding
import com.mazeppa.firebasechat.model.User

/**
 * Rashad Musayev on 5/1/2023 - 12:20
 */
class UserAdapter(
    private val users: ArrayList<User>,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemProfileBinding.inflate(LayoutInflater.from(parent.context))
        return UserViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.textViewUserName.text = user.name
        println("Profile Image ${user.profileImage}")
        holder.binding.imageViewProfileImage.load(user.profileImage)
    }
}