package it.polito.timebankingapp.ui.chats.chatslist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.ui.chats.chat.ChatViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {

    private lateinit var rv: RecyclerView
    private lateinit var adTmp: ChatListViewAdapter
    private val chatListViewModel: ChatListViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()

    private val profileVm: ProfileViewModel by activityViewModels()


    private lateinit var ivEmptyChats: ImageView
    private lateinit var tvEmptyChats: TextView
    private lateinit var tvEmptyChats2: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var chatListType: String? = arguments?.getString("point_of_origin").toString()

        if (chatListType != Type.SPECIFIC) chatListType = Type.GLOBAL

        Log.d("ChatsListFragment", "type: $chatListType")

        rv = view.findViewById(R.id.recycler_chat_list)
        rv.layoutManager = LinearLayoutManager(context)

        adTmp = ChatListViewAdapter(listOf(), ::selectChat, chatListType)
        rv.adapter = adTmp


        setFragmentResultListener("chatFragment") { _, bundle ->
            val result = bundle.getBoolean("SNACKBAR")
            val msg = "Requester hasn't enough balance." +
                    "The request has been deleted"
            if(result) {
                val snackBar =
                    Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }

        }



        /*DONE (Distinguish graphically if the chat is a to_offerer or to_requester chat)  */
        chatListViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            //if(chatListViewModel.hasChatsListBeenCleared.value == true)
                //chatListViewModel.setIsClearedFlag(false)
            //else
                if(!isLoading) {
                    val chatList = if(chatListType == Type.GLOBAL) chatListViewModel.allChatList.value!! else chatListViewModel.timeSlotChatList.value!!
                    if (chatList.isEmpty())
                        showNoChatsMessage(view, true)
                    else {
                        showNoChatsMessage(view, false)
                        adTmp =
                            ChatListViewAdapter(
                                chatList,
                                ::selectChat/*, ::updateTimeSlotProfile*/,
                                chatListType
                            )
                        rv.adapter = adTmp
                    }
                }
        }

//        adTmp = ChatsListViewAdapter(tempChatsList)
//        rv.adapter = adTmp
    }

    private fun showNoChatsMessage(view: View, show: Boolean) {
        ivEmptyChats = view.findViewById<ImageView>(R.id.fragment_chat_list_iv_empty_chats)
        tvEmptyChats = view.findViewById<TextView>(R.id.fragment_chat_list_tv_empty_chats)
        tvEmptyChats2 = view.findViewById<TextView>(R.id.fragment_chat_list_tv_empty_chats_2)

        when (show) {
            true -> {
                ivEmptyChats.visibility = View.VISIBLE
                tvEmptyChats.visibility = View.VISIBLE
                tvEmptyChats2.visibility = View.VISIBLE
            }
            else -> {
                ivEmptyChats.visibility = View.GONE
                tvEmptyChats.visibility = View.GONE
                tvEmptyChats2.visibility = View.GONE

            }
        }

    }

    fun selectChat(chat: Chat) {
        //chat.unreadMsgs = 0
        chatViewModel.selectChatFromChatList(chat)
    }

    companion object Type {
        val GLOBAL = "global"
        val SPECIFIC = "specific"

    }

    override fun onDetach() {
        //chatListViewModel.clearChatList()
        //chatListViewModel.setIsClearedFlag(true)
        super.onDetach()
    }






}