package au.edu.utas.pborana.interiorquote

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.pborana.interiorquote.databinding.ItemWindowBinding

class WindowAdapter(
    private val windows: MutableList<Window>,
    private val houseId: String,
    private val roomId: String
) : RecyclerView.Adapter<WindowAdapter.WindowViewHolder>() {

    inner class WindowViewHolder(val ui: ItemWindowBinding) :
        RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WindowViewHolder {
        val ui = ItemWindowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WindowViewHolder(ui)
    }

    override fun onBindViewHolder(holder: WindowViewHolder, position: Int) {
        val window = windows[position]

        holder.ui.txtWindowName.text = window.name

        if (window.productName.isNotEmpty()) {
            holder.ui.txtWindowSize.text =
                "${window.width} x ${window.height} mm\nProduct: ${window.productName} - ${window.productColour}"
        } else {
            holder.ui.txtWindowSize.text =
                "${window.width} x ${window.height} mm\nProduct: Not selected"
        }

        holder.ui.btnEditWindow.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditWindowActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            intent.putExtra("ROOM_ID", roomId)
            intent.putExtra("WINDOW_ID", window.id)
            intent.putExtra("WINDOW_NAME", window.name)
            intent.putExtra("WINDOW_WIDTH", window.width)
            intent.putExtra("WINDOW_HEIGHT", window.height)
            intent.putExtra("WINDOW_PRODUCT_NAME", window.productName)
            intent.putExtra("WINDOW_PRODUCT_PRICE", window.productPricePerSqm)
            intent.putExtra("WINDOW_PRODUCT_COLOUR", window.productColour)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = windows.size
}