package au.edu.utas.pborana.interiorquote

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddEditFloorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var floorId: String? = null

    private val productList = mutableListOf<Product>()

    private var selectedProductName = ""
    private var selectedProductPrice = 100.0
    private var selectedProductColour = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_floor)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        floorId = intent.getStringExtra("FLOOR_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)
        val txtName = findViewById<EditText>(R.id.txtFloorName)
        val txtWidth = findViewById<EditText>(R.id.txtWidth)
        val txtDepth = findViewById<EditText>(R.id.txtDepth)
        val btnSelectProduct = findViewById<Button>(R.id.btnSelectProduct)
        val btnSave = findViewById<Button>(R.id.btnSaveFloor)
        val btnDelete = findViewById<Button>(R.id.btnDeleteFloor)

        btnBack.setOnClickListener { finish() }

        btnSelectProduct.setOnClickListener {
            showProductDialog(btnSelectProduct)
        }

        if (floorId != null) {
            txtTitle.text = "Edit Floor Space"
            btnSave.text = "Update Floor Space"
            btnDelete.visibility = View.VISIBLE

            txtName.setText(intent.getStringExtra("FLOOR_NAME"))
            txtWidth.setText(intent.getDoubleExtra("FLOOR_WIDTH", 0.0).toString())
            txtDepth.setText(intent.getDoubleExtra("FLOOR_DEPTH", 0.0).toString())

            selectedProductName = intent.getStringExtra("FLOOR_PRODUCT_NAME") ?: ""
            selectedProductPrice = intent.getDoubleExtra("FLOOR_PRODUCT_PRICE", 100.0)
            selectedProductColour = intent.getStringExtra("FLOOR_PRODUCT_COLOUR") ?: ""

            if (selectedProductName.isNotEmpty()) {
                btnSelectProduct.text =
                    "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"
            }
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val depth = txtDepth.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || depth == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedProductName.isEmpty()) {
                Toast.makeText(this, "Select a product first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) {
                Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val extra = btnSelectProduct.tag as? Triple<String, String, String>

            val floor = FloorSpace(
                name = name,
                width = width,
                depth = depth,
                productName = selectedProductName,
                productDescription = extra?.first ?: "",
                productPricePerSqm = selectedProductPrice,
                productImageUrl = extra?.second ?: "",
                productColour = selectedProductColour
            )

            val ref = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")
                .document(roomId!!)
                .collection("floors")

            if (floorId == null) {
                ref.add(floor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Floor space saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving floor space", Toast.LENGTH_SHORT).show()
                    }
            } else {
                ref.document(floorId!!)
                    .set(floor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Floor space updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating floor space", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDelete.setOnClickListener {
            if (houseId == null || roomId == null || floorId == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Floor Space")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .collection("floors")
                        .document(floorId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Floor space deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadProductsFromAPI()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadProductsFromAPI() {
        Thread {
            try {
                val url = java.net.URL("https://utasbot.dev/kit305_2026/product?category=floor")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"

                val reader = java.io.BufferedReader(
                    java.io.InputStreamReader(connection.inputStream)
                )

                val response = reader.readText()
                val jsonObject = org.json.JSONObject(response)
                val jsonArray = jsonObject.getJSONArray("data")

                val tempList = mutableListOf<Product>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val variants = obj.getJSONArray("variants")

                    val colours = mutableListOf<String>()
                    for (j in 0 until variants.length()) {
                        colours.add(variants.getString(j))
                    }

                    val product = Product(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = obj.getString("category"),
                        description = obj.getString("description"),
                        pricePerSqm = obj.getDouble("price_per_sqm"),
                        imageUrl = obj.getString("imageUrl"),
                        colours = colours,
                        minWidth = 0.0,
                        maxWidth = 0.0,
                        minHeight = 0.0,
                        maxHeight = 0.0,
                        maxPanels = 1
                    )

                    tempList.add(product)
                }

                runOnUiThread {
                    productList.clear()
                    productList.addAll(tempList)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "API failed to load floor products", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showProductDialog(btnSelectProduct: Button) {
        if (productList.isEmpty()) {
            Toast.makeText(this, "Products still loading, try again", Toast.LENGTH_SHORT).show()
            return
        }

        val productNames = productList.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Floor Product")
            .setItems(productNames) { _, which ->

                val product = productList[which]
                val colours = product.colours.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Select Colour")
                    .setItems(colours) { _, colourIndex ->

                        selectedProductName = product.name
                        selectedProductPrice = product.pricePerSqm
                        selectedProductColour = colours[colourIndex]

                        btnSelectProduct.text =
                            "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"

                        btnSelectProduct.tag = Triple(
                            product.description,
                            product.imageUrl,
                            selectedProductColour
                        )
                    }
                    .show()
            }
            .show()
    }
}