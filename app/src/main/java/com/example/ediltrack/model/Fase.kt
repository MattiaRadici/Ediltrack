package com.example.ediltrack.model

import kotlinx.serialization.Serializable

@Serializable
data class Fase (
    var id: Int = 0,
    val titolo: String = "",
    val numeroFase: Int = 0,
    val descrizione: String = "",
    val cantiere: Int = 0,
    var terminata: Boolean = false
){
    constructor() : this (0,"",0,"",0)
}

