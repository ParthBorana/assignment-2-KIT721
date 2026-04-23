package au.edu.utas.pborana.interiorquote

import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore.getInstance
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    val db = getInstance()
    private val houses = mutableListOf<House>()
    private lateinit var rvHouses: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // RecyclerView setup
        rvHouses = findViewById(R.id.rvHouses)
        rvHouses.layoutManager = LinearLayoutManager(this)
        rvHouses.adapter = HouseAdapter(houses)

        // Add House button
        findViewById<android.widget.Button>(R.id.btnAddHouse).setOnClickListener {
            val intent = Intent(this, AddEditHouseActivity::class.java)
            startActivity(intent)
        }

        // Load houses from Firebase
        loadHouses()

        /* TOOK HINT FrOM CHATGPT how i can do success and failure listner so i can test firebase working or not

        val house = hashMapOf(
            "name" to "Test House",
            "address" to "Hobart"
        )

        db.collection("houses")
            .add(house)
            .addOnSuccessListener {
                Toast.makeText(this, "House added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error adding house", Toast.LENGTH_SHORT).show()
            }

         */

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadHouses() {
        db.collection("houses")
            .get()
            .addOnSuccessListener { result ->
                houses.clear()

                for (document in result) {
                    val house = document.toObject(House::class.java)
                    house.id = document.id
                    houses.add(house)
                }

                rvHouses.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load houses", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadHouses()
    }
}