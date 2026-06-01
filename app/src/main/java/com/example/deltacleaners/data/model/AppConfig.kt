package com.example.deltacleaners.data.model

data class AppConfig(
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val forceUpdate: Boolean = false,
    val updateTitle: String = "Update Required",
    val updateMessage: String = "A new version of Delta Cleaners is available. Please update to continue using the app.",
    val playStoreUrl: String = "https://play.google.com/store/apps/details?id=com.example.deltacleaners"
)
