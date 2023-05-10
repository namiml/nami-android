package com.namiml.app.test

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.namiml.Nami
import com.namiml.NamiConfiguration
import com.namiml.NamiLogLevel
import com.namiml.app.test.ui.theme.TestNamiTheme
import com.namiml.campaign.NamiCampaign
import com.namiml.campaign.NamiCampaignManager
import com.namiml.demo.basicpaywallmgmt.GooglePlayBillingHelper
import com.namiml.demo.basicpaywallmgmt.GooglePlayPurchaseListener
import com.namiml.paywall.NamiPaywallManager
import java.util.*

const val LOG_TAG = "TestNami"

fun <VM : ViewModel> viewModelProviderFactoryOf(
    create: () -> VM,
): ViewModelProvider.Factory = SimpleFactory(create)

private class SimpleFactory<VM : ViewModel>(
    private val create: () -> VM,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vm = create()
        if (modelClass.isInstance(vm)) {
            @Suppress("UNCHECKED_CAST")
            return vm as T
        }
        throw IllegalArgumentException("Can not create ViewModel for class: $modelClass")
    }
}

class MainActivity : ComponentActivity() {

    private var campaigns by mutableStateOf<List<NamiCampaign>>(listOf())
    internal val purchasesUpdatedListener = GooglePlayPurchaseListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isTelevision = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        requestedOrientation =
            if (isTelevision) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        var appPlatformId = BuildConfig.APP_PLATFORM_ID

        val AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv"

        if (getPackageManager().hasSystemFeature(AMAZON_FEATURE_FIRE_TV)) {
            appPlatformId = BuildConfig.APP_PLATFORM_ID_AMAZON
        }

        Nami.configure(
            NamiConfiguration.build(this, appPlatformId) {
                logLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.WARN
                if (BuildConfig.NAMI_ENV_PROD == false) {
                    settingsList = listOf("useStagingAPI")
                }
//                namiLanguageCode = NamiLanguageCode.DE
            },
        )

        setContent {
            TestNamiTheme(production = BuildConfig.NAMI_ENV_PROD) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    TestApp(isTelevision, campaigns)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        GooglePlayBillingHelper.connectToGooglePlay(applicationContext, purchasesUpdatedListener)

        // Only implemented for Paywalls-only plans. If on a Paywalls+Subscription Management plan
        // Nami takes care of these details (and more!) on your behalf.
        NamiPaywallManager.registerBuySkuHandler { paywall, sku ->
            Log.d(LOG_TAG, "registerBuySkuHandler ${sku.skuId}")

            purchasesUpdatedListener.paywall = paywall
            purchasesUpdatedListener.sku = sku

            val productList =
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku.skuId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                )

            val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

            purchasesUpdatedListener.billingClient?.queryProductDetailsAsync(params.build()) { billingResult,
                    productDetailsList, ->
                // Process the result
                Log.d(LOG_TAG, "billingResult $billingResult")

                val offerToken = productDetailsList[0].subscriptionOfferDetails?.get(0)?.offerToken

                val productDetailsParamsList: List<BillingFlowParams.ProductDetailsParams>
                if (offerToken != null) {
                    productDetailsParamsList =
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetailsList[0])
                                .setOfferToken(offerToken)
                                .build(),
                        )
                } else {
                    productDetailsParamsList =
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetailsList[0])
                                .build(),

                        )
                }

                val billingFlowParams =
                    BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                val billingResult = purchasesUpdatedListener.billingClient?.launchBillingFlow(
                    paywall,
                    billingFlowParams,
                )
            }
        }

        campaigns = NamiCampaignManager.allCampaigns()

        NamiCampaignManager.registerAvailableCampaignsHandler {
            campaigns = it
        }
    }
}

@Composable
fun TestApp(leanback: Boolean, campaigns: List<NamiCampaign>) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "Test Nami",
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.Black.takeIf {
                    BuildConfig.NAMI_ENV_PROD == true
                } ?: Color.White,
                modifier = Modifier.padding(start = 14.dp, top = 8.dp).takeIf {
                    leanback
                } ?: Modifier.padding(
                    0.dp,
                ),
            )
        },
        bottomBar = { BottomNavigationBar(navController, leanback) },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                Navigation(
                    navController = navController,
                    leanback = leanback,
                    campaigns = campaigns,
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background, // Set background color to avoid the white flashing when you switch between screens
    )
}

@Composable
fun Navigation(navController: NavHostController, leanback: Boolean, campaigns: List<NamiCampaign>) {
    NavHost(navController, startDestination = NavigationItem.Campaigns.route) {
        composable(NavigationItem.Campaigns.route) {
            CampaignView(leanback, campaigns)
        }
        composable(NavigationItem.Profile.route) {
            ProfileView(leanback)
        }
        composable(NavigationItem.Entitlements.route) {
            EntitlementsView(leanback)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TestNamiTheme {
        TestApp(leanback = false, listOf())
    }
}
