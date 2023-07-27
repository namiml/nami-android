package com.namiml.app.test

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.namiml.campaign.LaunchCampaignResult
import com.namiml.campaign.NamiCampaign
import com.namiml.campaign.NamiCampaignManager
import com.namiml.customer.NamiCustomerManager
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

        val initialConfigStringFromFile = readInitialConfigFromAsset(this@MainActivity)

        Nami.configure(
            NamiConfiguration.build(this, appPlatformId) {
                logLevel = NamiLogLevel.DEBUG.takeIf { BuildConfig.DEBUG } ?: NamiLogLevel.WARN
                initialConfig = initialConfigStringFromFile
//                namiLanguageCode = NamiLanguageCode.DE
            },
        )

        val profileViewModel = ProfileViewModel()

        val action: String? = intent?.action
        val data: Uri? = intent?.data

        if (action != null && data != null) {
            Log.d(LOG_TAG, "Attempting to launch deeplink campaign from uri  $data")

            NamiCampaignManager.launch(this, "", null, null, data) { result ->
                when (result) {
                    is LaunchCampaignResult.Success -> {
                        Log.d(LOG_TAG, "Deeplink Launch Campaign Success")
                    }
                    is LaunchCampaignResult.Failure -> {
                        Log.d(LOG_TAG, "Deeplink Launch Campaign Error -> ${result.error}")
                    }
                }
            }

            NamiCampaignManager.launch(this, "", null, null, data) { result ->
                when (result) {
                    is LaunchCampaignResult.Success -> {
                        Log.d(LOG_TAG, "Deeplink Launch Campaign Success")
                    }
                    is LaunchCampaignResult.Failure -> {
                        Log.d(LOG_TAG, "Deeplink Launch Campaign Error -> ${result.error}")
                    }
                }
            }
        }

        setContent {
            TestNamiTheme(production = BuildConfig.NAMI_ENV_PROD) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    TestApp(isTelevision, campaigns, profileViewModel)
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
            Log.d(LOG_TAG, "registerBuySkuHandler ${sku.skuId} ${sku.promoId}")

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

            purchasesUpdatedListener.billingClient?.queryProductDetailsAsync(
                params.build(),
            ) { billingResult,
                productDetailsList,
                ->
                // Process the result
                Log.d(LOG_TAG, "billingResult $billingResult $productDetailsList")

                productDetailsList?.firstOrNull().let {
                    if (it?.productType == BillingClient.ProductType.SUBS) {
                        var offer = it?.subscriptionOfferDetails?.firstOrNull { offer -> sku.promoId != null && offer?.offerId == sku.promoId }

                        if (offer == null) {
                            offer = it?.subscriptionOfferDetails?.firstOrNull { offer -> offer.offerId != null }
                        }
                        if (offer != null) {
                            Log.d(
                                LOG_TAG,
                                "We have an offer ${offer.offerId} on ${offer.basePlanId}",
                            )
                        } else {
                            Log.d(
                                LOG_TAG,
                                "We do not an offer for ${sku.skuId}}",
                            )
                        }
                        var productDetailsParamsList = if (offer != null) {
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(it)
                                    .setOfferToken(offer.offerToken)
                                    .build(),
                            )
                        } else {
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(it)
                                    .build(),
                            )
                        }

                        val billingFlowParams =
                            BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()

                        val billingResult =
                            purchasesUpdatedListener.billingClient?.launchBillingFlow(
                                paywall,
                                billingFlowParams,
                            )
                        Log.d(LOG_TAG, "final billingResult $billingResult")
                    }
                }
            }
        }

        campaigns = NamiCampaignManager.allCampaigns()

        NamiCampaignManager.registerAvailableCampaignsHandler {
            campaigns = it
        }

//        NamiPaywallManager.registerDeepLinkHandler { paywall, url ->
//            NamiPaywallManager.dismiss(paywall)
//
//            try {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//            } catch (e: ActivityNotFoundException) {
//                Log.d(LOG_TAG, "Unable to open deeplink uri $url")
//            }
//        }

        NamiPaywallManager.registerRestoreHandler { paywall ->
            Log.d(LOG_TAG, "Restore on paywall called.")
        }

        NamiCustomerManager.setCustomerDataPlatformId("12345")

        NamiCustomerManager.setCustomerAttribute("firstName", "Taylor")
    }
}

@Composable
fun TestApp(leanback: Boolean, campaigns: List<NamiCampaign>, profileViewModel: ProfileViewModel) {
    val navController = rememberNavController()
    val inAnonymousMode by profileViewModel.inAnonymousModeFlow.collectAsState()
    var onClickHasExecuted by remember { mutableStateOf(inAnonymousMode) }

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
                actions = {
                    Button(onClick = {
                        val switchAnonymousMode = !inAnonymousMode

                        if (switchAnonymousMode != onClickHasExecuted) {
                            onClickHasExecuted = switchAnonymousMode
                            NamiCustomerManager.setAnonymousMode(switchAnonymousMode)
                        }
                    }) {
                        Text(
                            "Turn off Anonymous Mode".takeIf { inAnonymousMode == true } ?: "Turn on Anonymous Mode",
                            color = MaterialTheme.colors.background,
                        )
                    }
                },
            )
        },
        bottomBar = { BottomNavigationBar(navController, leanback) },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                Navigation(
                    navController = navController,
                    leanback = leanback,
                    campaigns = campaigns,
                    profileViewModel = profileViewModel,

                )
            }
        },
        backgroundColor = MaterialTheme.colors.background, // Set background color to avoid the white flashing when you switch between screens
    )
}

@Composable
fun Navigation(
    navController: NavHostController,
    leanback: Boolean,
    campaigns: List<NamiCampaign>,
    profileViewModel: ProfileViewModel,
) {
    NavHost(navController, startDestination = NavigationItem.Campaigns.route) {
        composable(NavigationItem.Campaigns.route) {
            CampaignView(leanback, campaigns)
        }
        composable(NavigationItem.Profile.route) {
            ProfileView(leanback, profileViewModel = profileViewModel)
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
        TestApp(leanback = false, listOf(), profileViewModel = ProfileViewModel())
    }
}
