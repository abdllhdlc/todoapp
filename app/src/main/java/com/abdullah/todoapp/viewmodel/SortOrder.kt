package com.abdullah.todoapp.viewmodel

enum class SortOrder(val displayName: String) {
    CREATED_AT_DESC("En Yeni"),
    CREATED_AT_ASC("En Eski"),
    DUE_DATE_ASC("Son Tarih (Yakın)"),
    DUE_DATE_DESC("Son Tarih (Uzak)"),
    PRIORITY_HIGH("Öncelik (Yüksek)"),
    PRIORITY_LOW("Öncelik (Düşük)");

    companion object {
        fun fromString(value: String): SortOrder {
            return values().find { it.name == value } ?: CREATED_AT_DESC
        }
    }
} 