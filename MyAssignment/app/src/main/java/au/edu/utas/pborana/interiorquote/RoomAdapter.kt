package au.edu.utas.pborana.interiorquote

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.pborana.interiorquote.databinding.ItemRoomBinding
import android.net.Uri

class RoomAdapter(
    private val rooms: MutableList<Room>,
    private val houseId: String
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(val ui: ItemRoomBinding) :
        RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val ui = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(ui)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]

        holder.ui.txtRoomName.text = room.name
        holder.ui.txtRoomType.text = room.type
        holder.ui.txtRoomSize.text = "${room.size} sqm"

        holder.ui.btnEditRoom.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditRoomActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            intent.putExtra("ROOM_ID", room.id)
            intent.putExtra("ROOM_NAME", room.name)
            intent.putExtra("ROOM_TYPE", room.type)
            intent.putExtra("ROOM_SIZE", room.size)
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RoomDetailActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            intent.putExtra("ROOM_ID", room.id)
            intent.putExtra("ROOM_NAME", room.name)
            holder.itemView.context.startActivity(intent)
        }

        val imageView = holder.ui.imgRoomItem

        if (room.imageUri.isNotEmpty()) {
            imageView.setImageURI(Uri.parse(room.imageUri))
        } else {
            imageView.setImageResource(android.R.color.darker_gray)
        }
    }

    override fun getItemCount(): Int {
        return rooms.size
    }
}