package au.edu.utas.pborana.interiorquote

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

class AddEditWindowActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var windowId: String? = null

    private val productList = mutableListOf<Product>()

    private var selectedProductName = ""
    private var selectedProductDescription = ""
    private var selectedProductPrice = 50.0
    private var selectedProductImageUrl = ""
    private var selectedProductColour = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_window)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        windowId = intent.getStringExtra("WINDOW_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtName = findViewById<EditText>(R.id.txtWindowName)
        val txtWidth = findViewById<EditText>(R.id.txtWidth)
        val txtHeight = findViewById<EditText>(R.id.txtHeight)
        val btnSelectProduct = findViewById<Button>(R.id.btnSelectProduct)
        val btnSave = findViewById<Button>(R.id.btnSaveWindow)
        val btnDelete = findViewById<Button>(R.id.btnDeleteWindow)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)

        btnBack.setOnClickListener {
            finish()
        }

        btnSelectProduct.setOnClickListener {
            showProductDialog(btnSelectProduct)
        }

        if (windowId != null) {
            txtTitle.text = "Edit Window"
            btnSave.text = "Update Window"
            btnDelete.visibility = View.VISIBLE

            txtName.setText(intent.getStringExtra("WINDOW_NAME"))
            txtWidth.setText(intent.getDoubleExtra("WINDOW_WIDTH", 0.0).toString())
            txtHeight.setText(intent.getDoubleExtra("WINDOW_HEIGHT", 0.0).toString())

            selectedProductName = intent.getStringExtra("WINDOW_PRODUCT_NAME") ?: ""
            selectedProductDescription = intent.getStringExtra("WINDOW_PRODUCT_DESCRIPTION") ?: ""
            selectedProductPrice = intent.getDoubleExtra("WINDOW_PRODUCT_PRICE", 50.0)
            selectedProductImageUrl = intent.getStringExtra("WINDOW_PRODUCT_IMAGE_URL") ?: ""
            selectedProductColour = intent.getStringExtra("WINDOW_PRODUCT_COLOUR") ?: ""

            if (selectedProductName.isNotEmpty()) {
                btnSelectProduct.text = "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"
            }
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val height = txtHeight.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || height == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedProductName.isEmpty()) {
                Toast.makeText(this, "Select a product first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validateSelectedProduct(width, height)) {
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) {
                Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val window = Window(
                name = name,
                width = width,
                height = height,
                productName = selectedProductName,
                productDescription = selectedProductDescription,
                productPricePerSqm = selectedProductPrice,
                productImageUrl = selectedProductImageUrl,
                productColour = selectedProductColour
            )

            val ref = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")
                .document(roomId!!)
                .collection("windows")

            if (windowId == null) {
                ref.add(window)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Window saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving window", Toast.LENGTH_SHORT).show()
                    }
            } else {
                ref.document(windowId!!)
                    .set(window)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Window updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating window", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDelete.setOnClickListener {
            if (houseId == null || roomId == null || windowId == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Window")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .collection("windows")
                        .document(windowId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Window deleted", Toast.LENGTH_SHORT).show()
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
                val url = java.net.URL("https://utasbot.dev/kit305_2026/product?category=window")
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
                        minWidth = obj.optDouble("min_width", 0.0),
                        maxWidth = obj.optDouble("max_width", 0.0),
                        minHeight = obj.optDouble("min_height", 0.0),
                        maxHeight = obj.optDouble("max_height", 0.0),
                        maxPanels = obj.optInt("max_panels", 1)
                    )

                    tempList.add(product)
                }

                runOnUiThread {
                    productList.clear()
                    productList.addAll(tempList)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "API failed to load products", Toast.LENGTH_SHORT).show()
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
            .setTitle("Select Product")
            .setItems(productNames) { _, which ->
                val width = findViewById<EditText>(R.id.txtWidth).text.toString().toDoubleOrNull()
                val height = findViewById<EditText>(R.id.txtHeight).text.toString().toDoubleOrNull()

                if (width == null || height == null) {
                    Toast.makeText(this, "Enter width and height first", Toast.LENGTH_SHORT).show()
                    return@setItems
                }

                val product = productList[which]

                if (!validateProduct(product, width, height)) {
                    return@setItems
                }

                val colours = product.colours.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Select Colour")
                    .setItems(colours) { _, colourIndex ->
                        selectedProductName = product.name
                        selectedProductDescription = product.description
                        selectedProductPrice = product.pricePerSqm
                        selectedProductImageUrl = product.imageUrl
                        selectedProductColour = colours[colourIndex]

                        btnSelectProduct.text =
                            "$selectedProductName ($selectedProductPrice/m²) - $selectedProductColour"
                    }
                    .show()
            }
            .show()
    }

    private fun validateSelectedProduct(width: Double, height: Double): Boolean {
        val product = productList.find { it.name == selectedProductName }

        if (product == null) {
            Toast.makeText(this, "Product data not loaded. Select product again.", Toast.LENGTH_SHORT).show()
            return false
        }

        return validateProduct(product, width, height)
    }

    private fun validateProduct(product: Product, width: Double, height: Double): Boolean {
        if (height < product.minHeight || height > product.maxHeight) {
            Toast.makeText(
                this,
                "Height must be ${product.minHeight.toInt()}–${product.maxHeight.toInt()} mm",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (product.minWidth == product.maxWidth) {
            return validateRigidIncrement(width, product.minWidth, product.maxPanels, product.name)
        }

        if (product.maxPanels <= 1) {
            return validateDirectFit(width, product.minWidth, product.maxWidth, product.name)
        }

        return validateMultiPanel(width, product.minWidth, product.maxWidth, product.maxPanels, product.name)
    }

    private fun validateDirectFit(width: Double, minWidth: Double, maxWidth: Double, productName: String): Boolean {
        if (width < minWidth || width > maxWidth) {
            Toast.makeText(
                this,
                "$productName width must be ${minWidth.toInt()}–${maxWidth.toInt()} mm",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun validateMultiPanel(width: Double, minWidth: Double, maxWidth: Double, maxPanels: Int, productName: String): Boolean {
        if (width < minWidth) {
            Toast.makeText(this, "$productName width must be at least ${minWidth.toInt()} mm", Toast.LENGTH_SHORT).show()
            return false
        }

        if (width <= maxWidth) {
            return true
        }

        val requiredPanels = ceil(width / maxWidth).toInt()

        if (requiredPanels > maxPanels) {
            Toast.makeText(this, "$productName needs $requiredPanels panels, max allowed is $maxPanels", Toast.LENGTH_SHORT).show()
            return false
        }

        val panelWidth = width / requiredPanels

        if (panelWidth < minWidth || panelWidth > maxWidth) {
            Toast.makeText(this, "$productName cannot split into valid panel widths", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validateRigidIncrement(width: Double, panelWidth: Double, maxPanels: Int, productName: String): Boolean {
        for (panels in 1..maxPanels) {
            if (width == panelWidth * panels) {
                return true
            }
        }

        Toast.makeText(
            this,
            "$productName only supports exact widths like ${(panelWidth).toInt()}, ${(panelWidth * 2).toInt()}, or ${(panelWidth * 3).toInt()} mm",
            Toast.LENGTH_SHORT
        ).show()
        return false
    }
}