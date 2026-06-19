package com.example.ediltrack.model
import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class Utente (
    var id: Int = 0,
    val uid: String = "",
    val nome: String = "",
    val cognome: String = "",
    val ruolo: Int = 0,
    val img: String? = "",
)