package red.line.pet.presentation.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import red.line.pet.core.di.AppContainer
import red.line.pet.core.navigation.RedLineBottomNavBar
import red.line.pet.core.navigation.Screen
import red.line.pet.core.theme.RedLinePetTheme
import red.line.pet.domain.repository.AppThemeMode
import red.line.pet.presentation.expenses.ExpensesScreen
import red.line.pet.presentation.expenses.ExpensesViewModel
import red.line.pet.presentation.health.HealthScreen
import red.line.pet.presentation.health.HealthViewModel
import red.line.pet.presentation.passport.MedicalPassportScreen
import red.line.pet.presentation.passport.MedicalPassportViewModel
import red.line.pet.presentation.passport.MedicalPassportViewModelFactory
import red.line.pet.presentation.pets.PetsScreen
import red.line.pet.presentation.pets.PetsViewModel
import red.line.pet.presentation.reminders.RemindersScreen
import red.line.pet.presentation.reminders.RemindersViewModel
import red.line.pet.presentation.reminders.RemindersViewModelFactory
import red.line.pet.presentation.settings.SettingsScreen
import red.line.pet.presentation.settings.SettingsViewModel
import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import red.line.pet.presentation.onboarding.OnboardingScreen
import red.line.pet.presentation.splash.SplashScreen
import red.line.pet.presentation.vip.FreeTierNoticeDialog
import red.line.pet.presentation.vip.TrialExpiredLockScreen

@Composable
fun MainApp(
    appContainer: AppContainer
) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            userPreferencesRepository = appContainer.userPreferencesRepository,
            freeVipManager = appContainer.freeVipManager,
            checkFeatureAccessUseCase = appContainer.checkFeatureAccessUseCase
        )
    )
    val settingsState by settingsViewModel.uiState.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (settingsState.themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    RedLinePetTheme(darkTheme = isDark, forceRtl = true) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            MainScreen(appContainer = appContainer, settingsViewModel = settingsViewModel)
        }
    }
}

@Composable
fun MainScreen(
    appContainer: AppContainer,
    settingsViewModel: SettingsViewModel
) {
    var showSplash by remember { mutableStateOf(true) }
    var showOnboarding by remember { mutableStateOf(false) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    if (showOnboarding) {
        OnboardingScreen(onFinish = { showOnboarding = false })
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Pets.route

    val petsViewModel: PetsViewModel = viewModel(
        factory = PetsViewModel.Factory(
            appContainer.getPetsUseCase,
            appContainer.addPetUseCase,
            appContainer.deletePetUseCase,
            appContainer.updatePetUseCase,
            appContainer.checkFeatureAccessUseCase
        )
    )

    val healthViewModel: HealthViewModel = viewModel(
        factory = HealthViewModel.Factory(
            appContainer.getHealthRecordsUseCase,
            appContainer.getUpcomingRemindersUseCase,
            appContainer.addHealthRecordUseCase,
            appContainer.getPetsUseCase,
            appContainer.checkFeatureAccessUseCase
        )
    )

    val expensesViewModel: ExpensesViewModel = viewModel(
        factory = ExpensesViewModel.Factory(
            appContainer.getPetsUseCase,
            appContainer.getExpensesUseCase,
            appContainer.getExpenseSummaryUseCase,
            appContainer.addExpenseUseCase
        )
    )

    val foodViewModel: red.line.pet.presentation.food.FoodViewModel = viewModel(
        factory = red.line.pet.presentation.food.FoodViewModel.Factory(
            appContainer.getPetsUseCase,
            appContainer.getFoodSchedulesUseCase,
            appContainer.getTodayMealsUseCase,
            appContainer.addFoodScheduleUseCase,
            appContainer.updateFoodScheduleUseCase,
            appContainer.deleteFoodScheduleUseCase,
            appContainer.toggleMealCompletedUseCase,
            appContainer.getWeeklyFoodScheduleUseCase,
            appContainer.checkFeatureAccessUseCase
        )
    )

    val weightViewModel: red.line.pet.presentation.weight.WeightViewModel = viewModel(
        factory = red.line.pet.presentation.weight.WeightViewModel.Factory(
            appContainer.getPetsUseCase,
            appContainer.getWeightRecordsUseCase,
            appContainer.getFilteredWeightRecordsUseCase,
            appContainer.getWeightStatisticsUseCase,
            appContainer.addWeightRecordUseCase,
            appContainer.updateWeightRecordUseCase,
            appContainer.deleteWeightRecordUseCase,
            appContainer.checkFeatureAccessUseCase
        )
    )

    val memoriesViewModel: red.line.pet.presentation.memories.MemoriesViewModel = viewModel(
        factory = red.line.pet.presentation.memories.MemoriesViewModel.Factory(
            appContainer.getPetsUseCase,
            appContainer.getMemoriesUseCase,
            appContainer.searchMemoriesUseCase,
            appContainer.addMemoryUseCase,
            appContainer.updateMemoryUseCase,
            appContainer.deleteMemoryUseCase,
            appContainer.checkFeatureAccessUseCase
        )
    )

    val remindersViewModel: RemindersViewModel = viewModel(
        factory = RemindersViewModelFactory(
            appContainer = appContainer,
            context = LocalContext.current
        )
    )

    val medicalPassportViewModel: MedicalPassportViewModel = viewModel(
        factory = MedicalPassportViewModelFactory(appContainer)
    )

    val vipViewModel: red.line.pet.presentation.vip.VipViewModel = viewModel(
        factory = red.line.pet.presentation.vip.VipViewModel.Factory(
            getVipStatusUseCase = appContainer.getVipStatusUseCase,
            purchaseVipUseCase = appContainer.purchaseVipUseCase,
            processPurchaseResultUseCase = appContainer.processPurchaseResultUseCase,
            restorePurchasesUseCase = appContainer.restorePurchasesUseCase,
            billingManager = appContainer.myketBillingManager
        )
    )

    val backupRestoreViewModel: red.line.pet.presentation.backup.BackupRestoreViewModel = viewModel(
        factory = red.line.pet.presentation.backup.BackupRestoreViewModel.Factory(
            localBackupManager = appContainer.localBackupManager,
            freeVipManager = appContainer.freeVipManager
        )
    )

    val isVip by appContainer.freeVipManager.isVip.collectAsState(initial = false)
    val trialInfo by appContainer.freeTrialManager.trialInfo.collectAsState()
    var showFreeNoticeDialog by remember { mutableStateOf(true) }
    var showVipFromLock by remember { mutableStateOf(false) }

    // If 30-day trial is expired and user is not VIP, lock the application completely
    if (trialInfo.isTrialExpired && !isVip) {
        if (showVipFromLock) {
            red.line.pet.presentation.vip.VipUpgradeScreen(
                viewModel = vipViewModel,
                onNavigateBack = { showVipFromLock = false }
            )
        } else {
            val vipUiState by vipViewModel.uiState.collectAsState()
            TrialExpiredLockScreen(
                onPurchaseVipClick = {
                    showVipFromLock = true
                },
                onRestorePurchaseClick = {
                    vipViewModel.restorePurchases()
                },
                isLoading = vipUiState.flowState == red.line.pet.presentation.vip.PurchaseFlowState.PURCHASING,
                statusMessage = vipUiState.errorMessage ?: vipUiState.successMessage
            )
        }
        return
    }

    // Informational popup on app launch for free tier users
    if (!isVip && showFreeNoticeDialog) {
        FreeTierNoticeDialog(
            daysRemaining = trialInfo.daysRemaining,
            onDismiss = { showFreeNoticeDialog = false },
            onUpgradeVipClick = {
                showFreeNoticeDialog = false
                navController.navigate(Screen.VipUpgrade.route)
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            RedLineBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Pets.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(220)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(220)
                )
            }
        ) {
            composable(Screen.Pets.route) {
                PetsScreen(
                    viewModel = petsViewModel,
                    onNavigateToFood = { navController.navigate(Screen.Food.route) },
                    onNavigateToHealth = { navController.navigate(Screen.Health.route) },
                    onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                    onNavigateToWeight = { navController.navigate(Screen.Weight.route) },
                    onNavigateToMemories = { navController.navigate(Screen.Memories.route) },
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToMedicalPassport = { navController.navigate(Screen.MedicalPassport.route) },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.Health.route) {
                HealthScreen(
                    viewModel = healthViewModel,
                    onNavigateToPassport = { navController.navigate(Screen.MedicalPassport.route) },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(viewModel = expensesViewModel)
            }
            composable(Screen.Reminders.route) {
                RemindersScreen(
                    viewModel = remindersViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.MedicalPassport.route) {
                MedicalPassportScreen(
                    viewModel = medicalPassportViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.Food.route) {
                red.line.pet.presentation.food.FoodScreen(
                    viewModel = foodViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.Weight.route) {
                red.line.pet.presentation.weight.WeightScreen(
                    viewModel = weightViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.Memories.route) {
                red.line.pet.presentation.memories.MemoriesScreen(
                    viewModel = memoriesViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onShowOnboarding = { showOnboarding = true },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) },
                    onNavigateToBackupRestore = { navController.navigate(Screen.BackupRestore.route) }
                )
            }
            composable(Screen.VipUpgrade.route) {
                red.line.pet.presentation.vip.VipUpgradeScreen(
                    viewModel = vipViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.BackupRestore.route) {
                red.line.pet.presentation.backup.BackupRestoreScreen(
                    viewModel = backupRestoreViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVip = { navController.navigate(Screen.VipUpgrade.route) }
                )
            }
        }
    }
}
