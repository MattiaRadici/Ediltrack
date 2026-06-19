package com.example.ediltrack.model

import kotlinx.serialization.Serializable

@Serializable
data class Problematica (
    var id: Int?= null,
    val validazione: Int = 0,
    val descrizione: String = "",
    val cantiere:  Int? = null,
    val emittente: String? = null,
    val img_cantiere: String = "",
    val fase: Int = 0,
    val commento: String? = ""
) : java.io.Serializable