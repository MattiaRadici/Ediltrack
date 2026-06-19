package com.example.ediltrack.model.uimodel

data class CantiereUI(
    val id: Int = 0,
    val nome: String,
    val luogo: String,
    val img: String?,
    val numeroDipendenti: Int,
    val capocantiere: String,
    val dismesso: Boolean
){
    constructor() : this (0,"","","",0,"",false)

}