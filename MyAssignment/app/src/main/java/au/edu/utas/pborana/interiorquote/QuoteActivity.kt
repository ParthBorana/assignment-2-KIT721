package au.edu.utas.pborana.interiorquote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class QuoteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val items = mutableListOf<QuoteItem>()

    private lateinit var rvQuote: RecyclerView
    private lateinit var txtTotal: TextView

    private var houseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quote)

        houseId = intent.getStringExtra("HOUSE_ID")

        rvQuote = findViewById(R.id.rvQuoteItems)
        txtTotal = findViewById(R.id.txtTotalQuote)

        rvQuote.layoutManager = LinearLayoutManager(this)
        rvQuote.adapter = QuoteAdapter(items) {
            updateTotal()
        }

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnShareQuote).setOnClickListener {
            shareQuote()
        }

        loadQuoteData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadQuoteData() {
        if (houseId == null) return

        db.collection("houses")
            .document(houseId!!)
            .collection("rooms")
            .get()
            .addOnSuccessListener { roomsResult ->

                items.clear()

                for (roomDoc in roomsResult) {
                    val roomId = roomDoc.id
                    val roomName = roomDoc.getString("name") ?: "Room"

                    items.add(
                        QuoteItem(
                            name = "Labour - $roomName",
                            price = 200.0
                        )
                    )

                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId)
                        .collection("windows")
                        .get()
                        .addOnSuccessListener { windowsResult ->

                            for (windowDoc in windowsResult) {
                                val window = windowDoc.toObject(Window::class.java)
                                val area = (window.width * window.height) / 1_000_000
                                val price = area * window.productPricePerSqm

                                items.add(
                                    QuoteItem(
                                        name = "Window: ${window.name} - ${window.productName} (${window.productColour})",
                                        price = price
                                    )
                                )
                            }

                            db.collection("houses")
                                .document(houseId!!)
                                .collection("rooms")
                                .document(roomId)
                                .collection("floors")
                                .get()
                                .addOnSuccessListener { floorsResult ->

                                    for (floorDoc in floorsResult) {
                                        val floor = floorDoc.toObject(FloorSpace::class.java)
                                        val area = (floor.width * floor.depth) / 1_000_000
                                        val price = area * 100

                                        items.add(
                                            QuoteItem(
                                                name = "Floor: ${floor.name}",
                                                price = price
                                            )
                                        )
                                    }

                                    rvQuote.adapter?.notifyDataSetChanged()
                                    updateTotal()
                                }
                        }
                }
            }
    }

    private fun updateTotal() {
        val total = items
            .filter { it.isSelected }
            .sumOf { it.price }

        txtTotal.text = "Total: $" + String.format("%.2f", total)
    }

    private fun shareQuote() {
        val selectedItems = items.filter { it.isSelected }

        var quoteText = "Quote Summary\n\n"

        for (item in selectedItems) {
            quoteText += "${item.name}: $" + String.format("%.2f", item.price) + "\n"
        }

        val total = selectedItems.sumOf { it.price }
        quoteText += "\nTotal: $" + String.format("%.2f", total)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, quoteText)

        startActivity(Intent.createChooser(intent, "Share Quote"))
    }
}