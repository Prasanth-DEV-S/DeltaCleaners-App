package com.example.deltacleaners.ui.splash

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import com.example.deltacleaners.data.model.AppConfig
import com.example.deltacleaners.data.repository.ConfigRepository
import com.example.deltacleaners.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    suspend fun checkUpdate(): AppConfig? {
        val config = configRepository.getAppConfig() ?: return null
        val currentVersionCode = getCurrentVersionCode()
        
        return if (config.forceUpdate && config.latestVersionCode > currentVersionCode) {
            config
        } else {
            null
        }
    }

    private fun getCurrentVersionCode(): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getInitialRoute(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return com.example.deltacleaners.ui.navigation.Screen.Login.route
        
        val userResult = userRepository.getUser(currentUser.uid)
        return userResult.fold(
            onSuccess = { user ->
                if (user?.role == "cleaner") {
                    com.example.deltacleaners.ui.navigation.Screen.CleanerDashboard.route
                } else {
                    com.example.deltacleaners.ui.navigation.Screen.Home.route
                }
            },
            onFailure = {
                com.example.deltacleaners.ui.navigation.Screen.Login.route
            }
        )
    }
}
