package com.namiml.app.test

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Campaigns : NavigationItem(
        "Campaigns",
        R.drawable.ic_baseline_rocket_launch_24,
        "Campaigns"
    )
    object Profile : NavigationItem("Profile", R.drawable.ic_baseline_person_24, "Profile")
    object Entitlements : NavigationItem(
        "Entitlements",
        R.drawable.ic_baseline_diamond_24,
        "Entitlements"
    )
}
