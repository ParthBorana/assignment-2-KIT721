package au.edu.utas.pborana.interiorquote

import com.google.firebase.firestore.Exclude

data class House(
    @get:Exclude var id: String? = null,
    var name: String? = null,
    var address: String? = null,
    var customerName: String? = null
)