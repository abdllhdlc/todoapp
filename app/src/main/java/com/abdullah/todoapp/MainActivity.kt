package com.abdullah.todoapp

import android.Manifest
import android.app.Application
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abdullah.todoapp.ui.screens.ToDoScreen
import com.abdullah.todoapp.ui.screens.ToDoDetailScreen
import com.abdullah.todoapp.ui.theme.ModernToDoListTheme
import com.abdullah.todoapp.viewmodel.ThemeViewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Bildirim izinlerini kontrol et
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        Log.d("MainActivity", "Notification permission already granted")
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                        Log.d("MainActivity", "Should show notification permission rationale")
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Tam zamanlı alarm izinlerini kontrol et
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Kullanıcıyı ayarlara yönlendir
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            }

            // Geri tuşu yönetimi
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })

            setContent {
                val applicationContext = LocalContext.current.applicationContext as Application
                val themeViewModel: ThemeViewModel = viewModel(
                    factory = ThemeViewModel.provideFactory(applicationContext)
                )
                val themeMode by themeViewModel.themeMode.collectAsState()
                val navController = rememberNavController()

                // Bildirimden gelen todoId'yi kontrol et
                val todoId = intent.getLongExtra("todoId", -1L)
                val fromNotification = intent.getBooleanExtra("fromNotification", false)

                // Bildirimden geldiyse ve geçerli bir todoId varsa, detay sayfasına yönlendir
                LaunchedEffect(fromNotification, todoId) {
                    if (fromNotification && todoId != -1L) {
                        try {
                            Log.d("MainActivity", "Navigating to todo detail: $todoId")
                            navController.navigate("todo_detail/$todoId") {
                                popUpTo("todo_list") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error navigating to todo detail", e)
                        }
                    }
                }

                ModernToDoListTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "todo_list",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(0)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(0)
                                )
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(0)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(0)
                                )
                            }
                        ) {
                            composable("todo_list") {
                                ToDoScreen(
                                    onNavigateToDetail = { todoId ->
                                        navController.navigate("todo_detail/$todoId") {
                                            popUpTo("todo_list") {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    themeViewModel = themeViewModel
                                )
                            }
                            composable(
                                route = "todo_detail/{todoId}",
                                arguments = listOf(
                                    navArgument("todoId") {
                                        type = NavType.LongType
                                    }
                                )
                            ) { backStackEntry ->
                                val todoId = backStackEntry.arguments?.getLong("todoId")
                                if (todoId != null) {
                                    ToDoDetailScreen(
                                        todoId = todoId,
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
        }
    }
}