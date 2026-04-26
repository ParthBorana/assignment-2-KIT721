package au.edu.utas.pborana.interiorquote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.pborana.interiorquote.databinding.ItemQuoteBinding

class QuoteAdapter(
    private val items: MutableList<QuoteItem>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<QuoteAdapter.ViewHolder>() {

    inner class ViewHolder(val ui: ItemQuoteBinding) :
        RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val ui = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(ui)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Set name
        holder.ui.txtItemName.text = item.name

        // Set price
        holder.ui.txtItemPrice.text = "$" + String.format("%.2f", item.price)

        // Prevent checkbox glitch
        holder.ui.checkItem.setOnCheckedChangeListener(null)
        holder.ui.checkItem.isChecked = item.isSelected

        //  checkbox click
        holder.ui.checkItem.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onUpdate()
        }
    }

    override fun getItemCount(): Int = items.size
}