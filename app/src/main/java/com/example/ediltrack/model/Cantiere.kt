package com.example.ediltrack.model
import kotlinx.serialization.Serializable

@Serializable
data class Cantiere (
    var id: Int = 0,
    val luogo: String = "",
    val nome: String = "",
    val img_cantiere: String? = "",
    val dismesso: Boolean = false
){
    constructor() : this (0,"","","",false)
}