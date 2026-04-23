package au.edu.utas.pborana.interiorquote

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.pborana.interiorquote.databinding.ItemHouseBinding

class HouseAdapter(private val houses: MutableList<House>) :
    RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    inner class HouseViewHolder(val ui: ItemHouseBinding) :
        RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val ui = ItemHouseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HouseViewHolder(ui)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]
        holder.ui.txtHouseName.text = house.name
        holder.ui.txtHouseAddress.text = house.address
        holder.ui.txtCustomerName.text = house.customerName

        holder.ui.btnEditHouse.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditHouseActivity::class.java)
            intent.putExtra("HOUSE_ID", house.id)
            intent.putExtra("HOUSE_NAME", house.name)
            intent.putExtra("HOUSE_ADDRESS", house.address)
            intent.putExtra("CUSTOMER_NAME", house.customerName)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return houses.size
    }
}