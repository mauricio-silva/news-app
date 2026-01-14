package com.example.news_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.news_app.presentation.auth.AuthGateScreen
import com.example.news_app.presentation.detail.ArticleDetailScreen
import com.example.news_app.presentation.headlines.ui.HeadlinesScreen

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.AUTH) {
        composable(Routes.AUTH) {
            AuthGateScreen(
                onAuthorized = {
                    nav.navigate(Routes.HEADLINES) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HEADLINES) {
            HeadlinesScreen(
                onOpenArticle = { id ->
                    nav.navigate("${Routes.DETAIL}/$id")
                }
            )
        }
        composable(
            route = "${Routes.DETAIL}/{${Routes.DETAIL_ARG_ID}}",
            arguments = listOf(navArgument(Routes.DETAIL_ARG_ID) { type = NavType.StringType })
        ) {
            ArticleDetailScreen(onBack = { nav.popBackStack() })
        }
    }
}