package com.example.ediltrack.model
import kotlinx.serialization.Serializable

@Serializable
data class UtenteCantiere (
    val id: Int = 0,
    val id_cantiere: Int = 0,
    val uid: String = "",
    val operativo: Boolean = true
){
    constructor() : this(0,0,"", true)
}