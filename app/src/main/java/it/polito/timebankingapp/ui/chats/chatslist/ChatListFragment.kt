package it.polito.timebankingapp.ui.chats.chatslist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.ui.chats.chat.ChatViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel

class ChatListFragment : Fragment(R.layout.fragment_chats_list_list) {

    private lateinit var rv : RecyclerView
    private lateinit var adTmp: ChatListViewAdapter
    private val chatListViewModel : ChatListViewModel by activityViewModels()
    private val chatViewModel : ChatViewModel by activityViewModels()

    private val profileVm : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var chatListType: String ? = arguments?.getString("point_of_origin").toString()

        if(chatListType != Type.SPECIFIC) chatListType = Type.GLOBAL

        Log.d("ChatsListFragment", "type: $chatListType" )

        rv = view.findViewById(R.id.recycler_chat_list)
        rv.layoutManager = LinearLayoutManager(context)



        /*TODO(Distinguish graphically if the chat is a to_offerer or to_requester chat)  */
        chatListViewModel.chatsList.observe(viewLifecycleOwner){
            adTmp = ChatListViewAdapter(it, ::selectChat/*, ::updateTimeSlotProfile*/, chatListType)
            rv.adapter = adTmp
        }

//        adTmp = ChatsListViewAdapter(tempChatsList)
//        rv.adapter = adTmp
    }

    fun selectChat(chat : Chat){
        chat.unreadMsgs = 0
        chatViewModel.registerMessagesListener(chat)
    }

    companion object Type{
        val GLOBAL = "global"
        val SPECIFIC = "specific"

    }


}