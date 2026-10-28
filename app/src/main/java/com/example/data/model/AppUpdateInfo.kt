package com.example.data.model

data class AppUpdateInfo(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0",
    val releaseNotes: String = "",
    val apkUrl: String = "",
    val releaseDate: String = "",
    val isMandatory: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
