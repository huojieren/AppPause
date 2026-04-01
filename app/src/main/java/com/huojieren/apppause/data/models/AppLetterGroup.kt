package com.huojieren.apppause.data.models

data class AppLetterGroup<T>(
    val letter: String,
    val items: List<T>
)