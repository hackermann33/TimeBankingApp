package it.polito.timebankingapp.ui.chats.chatslist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.chat.ChatsListItem

class ChatsListViewAdapter(
    data: List<ChatsListItem>
) : RecyclerView.Adapter<ChatsListViewAdapter.ItemViewHolder>() {

    private var displayData = data.toMutableList()

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fullNameText: TextView = itemView.findViewById(R.id.chat_list_item_fullname)
        var messageText: TextView = itemView.findViewById(R.id.chat_list_item_message)
        var timeText: TextView = itemView.findViewById(R.id.chat_list_item_timestamp)
        var numNotifiesText: TextView = itemView.findViewById(R.id.chat_list_item_notifies_number)

        fun bind(cli: ChatsListItem, openChatAction: (v: View) -> Unit) {
            fullNameText.text = "Nome Cognome" //necessario riferimento usr o timeslotusr
            messageText.text = cli.chatMessages[cli.chatMessages.size-1].messageText
            timeText.text = cli.chatMessages[cli.chatMessages.size-1].timestamp.split("-")[1] //se è di oggi mostra l'orario, altrimenti la data
            numNotifiesText.text =  "(1)" //logica conteggio non letti da implementare in futuro
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
        holder.bind(item, openChatAction = {
            val destination = R.id.action_nav_chatsList_to_nav_chat

            Navigation.findNavController(it).navigate(
                destination//,
                //bundleOf("point_of_origin" to type, "userId" to item.userId)
            )
        });
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size
}