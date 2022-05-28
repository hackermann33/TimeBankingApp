package it.polito.timebankingapp.ui.chats.chatslist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.chat.ChatsListItem

class ChatListViewAdapter(
    private var data: List<ChatsListItem>,
    private var selectChat: (chat: ChatsListItem) -> Unit?,
    /*private var updateUser: (userId: String) -> Unit?,*/
    val type: String,
) : RecyclerView.Adapter<ChatListViewAdapter.ItemViewHolder>() {

    private var displayData = data.toMutableList()

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvOtherFullName: TextView = itemView.findViewById(R.id.chat_list_item_fullname)
        var tvLastMessage: TextView = itemView.findViewById(R.id.chat_list_item_last_message)
        var tvLastMessageTime: TextView = itemView.findViewById(R.id.chat_list_item_timestamp)
        var tvTimeSlotTitle: TextView = itemView.findViewById(R.id.chat_list_item_time_slot_title)

        private val civImagePic: CircleImageView = itemView.findViewById(R.id.chat_profile_pic)
        private val nUnreadMsg: TextView = itemView.findViewById(R.id.n_unread_msg)
        private val unreadMsgCard: CardView = itemView.findViewById(R.id.unread_msg_card)

        fun bind(cli: ChatsListItem, openChatAction: (v: View) -> Unit) {
//            fullNameText.text = "Nome Cognome" //necessario riferimento usr o timeslotusr
//            messageText.text = cli.chatMessages[cli.chatMessages.size-1].messageText
//            timeText.text = cli.chatMessages[cli.chatMessages.size-1].timestamp.split("-")[1] //se è di oggi mostra l'orario, altrimenti la data
//            numNotifiesText.text =  "(1)" //logica conteggio non letti da implementare in futuro
            tvOtherFullName.text = cli.otherUserName
            tvLastMessage.text = cli.lastMessageText
            if(cli.nUnreadMsg > 0)
                nUnreadMsg.text = cli.nUnreadMsg.toString()
            else
                unreadMsgCard.visibility = View.GONE
            tvLastMessageTime.text = cli.lastMessageTime
            tvTimeSlotTitle.text = cli.timeSlotTitle
            cli.lastMessageTime
            // sarebbe da mettere il last message della chat dentro il documento in userRooms (per l'anteprima)
            // e anche le altre info riguardo a tempo e conteggio non letti e foto profilo altro utente

            // Use Glide HERE!!!
            Helper.loadImageIntoView(civImagePic, cli.otherProfilePic)

            this.itemView.setOnClickListener(openChatAction)
        }


    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val destination =  R.layout.fragment_chats_list_item

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = displayData[position]
        holder.bind(item, openChatAction =
        {
            val destination = if(type == ChatListFragment.GLOBAL) R.id.action_nav_allChatsList_to_nav_chat
            else
                R.id.action_nav_timeSlotChatsList_to_nav_chat
            selectChat(item)
//            val b = bundleOf("profilePic" to item.userPic)
//            b.putString("profileName", item.userName)
//            b.putString("profileId", item.userId)
            //updateUser(item.userId)
            Navigation.findNavController(it).navigate(
                destination,
                //bundleOf("point_of_origin" to type, "userId" to item.userId)
            )
        }
        );
    }


    //how many items?
    override fun getItemCount(): Int = displayData.size
}