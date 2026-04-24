package au.edu.utas.pborana.interiorquote

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.pborana.interiorquote.databinding.ItemFloorBinding

class FloorAdapter(
    private val floors: MutableList<FloorSpace>,
    private val houseId: String,
    private val roomId: String
) : RecyclerView.Adapter<FloorAdapter.FloorViewHolder>() {

    inner class FloorViewHolder(val ui: ItemFloorBinding) :
        RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorViewHolder {
        val ui = ItemFloorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FloorViewHolder(ui)
    }

    override fun onBindViewHolder(holder: FloorViewHolder, position: Int) {
        val floor = floors[position]

        holder.ui.txtFloorName.text = floor.name
        holder.ui.txtFloorSize.text = "${floor.width} x ${floor.depth} mm"

        holder.ui.btnEditFloor.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditFloorActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            intent.putExtra("ROOM_ID", roomId)
            intent.putExtra("FLOOR_ID", floor.id)
            intent.putExtra("FLOOR_NAME", floor.name)
            intent.putExtra("FLOOR_WIDTH", floor.width)
            intent.putExtra("FLOOR_DEPTH", floor.depth)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = floors.size
}