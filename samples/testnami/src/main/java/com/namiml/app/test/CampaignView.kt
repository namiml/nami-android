package com.namiml.app.test

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.namiml.app.test.ui.theme.DarkGrey
import com.namiml.billing.NamiPurchaseState
import com.namiml.campaign.LaunchCampaignResult
import com.namiml.campaign.NamiCampaignManager
import com.namiml.paywall.model.NamiPaywallAction

data class CampaignHeader(
    var group: Number,
    var name: String
)

data class CampaignItem(
    val group: Number,
    val name: String,
    val type: String,
    val label: String?
)

val headers = listOf(
    CampaignHeader(1, "Basic"),
    CampaignHeader(2, "Templates"),
    CampaignHeader(3, "Branded"),
    CampaignHeader(4, "Other")
)

fun getHeader(headerGroup: Number): CampaignHeader {
    return headers.first { it.group.equals(headerGroup) }
}

val campaignItems = listOf(
    CampaignItem(1, "Default", "default", ""),
// CampaignItem(1,"Custom label...", "label", ""),
    CampaignItem(2, "Penguin", "label", "penguin"),
    CampaignItem(2, "Pacific", "label", "pacific"),
    CampaignItem(2, "Trident", "label", "trident"),
    CampaignItem(2, "Starfish", "label", "starfish"),
    CampaignItem(2, "Mantis", "label", "mantis"),
    CampaignItem(2, "Venice", "label", "venice"),
    CampaignItem(3, "Living Room", "label", "livingroom"),
    CampaignItem(4, "Legacy", "label", "classic")
// CampaignItem(3, "Linked Paywall", "label", "linked")
)

@Composable
fun CampaignTitleText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        color = Color.Black
    )
}

@Composable
fun CampaignSubtitleText(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        color = Color.Black
    )
}

@Composable
fun CampaignCard(title: String, subtitle: String) {
    BoxWithConstraints {
        if (maxWidth < 400.dp) {
            Column {
                CampaignTitleText(text = title)
                CampaignSubtitleText(text = subtitle)
            }
        } else {
            Row {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CampaignTitleText(text = title)
                    CampaignSubtitleText(text = subtitle)
                }
            }
        }
    }
}

@Composable
fun Header(name: String) {
    Text(
        text = name,
        fontSize = 10.sp,
        textAlign = TextAlign.Left,
        modifier = Modifier.padding(14.dp),
        color = DarkGrey
    )
}

@Composable
fun CampaignRow(campaign: CampaignItem) {
    val activity = LocalContext.current as Activity
    var color by remember { mutableStateOf(Color.Black) }

    TextButton(
        modifier = Modifier
            .onFocusChanged { color = if (it.isFocused) Color.LightGray else Color.White }
            .focusable(),
        onClick = {
            if (campaign.type == "default") {

                NamiCampaignManager.launch(activity, paywallActionCallback = { action, skuId ->
                    when (action) {
                        NamiPaywallAction.NAMI_BUY_SKU -> {
                            Log.d(LOG_TAG, "Buy SKU - ${skuId.orEmpty()}")
                        }
                        NamiPaywallAction.NAMI_SELECT_SKU -> {
                            Log.d(LOG_TAG, "Select SKU - ${skuId.orEmpty()}")
                        }
                        NamiPaywallAction.NAMI_RESTORE_PURCHASES -> {
                            Log.d(LOG_TAG, "Restore Purchases")
                        }
                        NamiPaywallAction.NAMI_SIGN_IN -> {
                            Log.d(LOG_TAG, "Sign in")
                        }
                        NamiPaywallAction.NAMI_CLOSE_PAYWALL -> {
                            Log.d(LOG_TAG, "Close Paywall")
                        }
                        NamiPaywallAction.NAMI_PURCHASE_SELECTED_SKU -> {
                            Log.d(LOG_TAG, "Purchase Selected SKU - ${skuId.orEmpty()}")
                        }
                    }
                }) { result ->
                    when (result) {
                        is LaunchCampaignResult.Success -> {
                            Log.d(LOG_TAG, "Launch Campaign Success")
                        }
                        is LaunchCampaignResult.Failure -> {
                            Log.d(LOG_TAG, "Launch Campaign Error -> ${result.error}")
                            Toast.makeText(
                                activity,
                                "Campaign Default Launch Error -> ${result.error}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is LaunchCampaignResult.PurchaseChanged -> {
                            Log.d(
                                LOG_TAG,
                                "Launch Campaign Result - Purchase changed -> ${result.purchaseState}"
                            )

                            if (result.purchaseState == NamiPurchaseState.PURCHASED) {
                                Log.d(
                                    LOG_TAG,
                                    "PurchasedState - Purchased -> ${result.activePurchases}"
                                )
                            }
                            if (result.purchaseState == NamiPurchaseState.CANCELLED) {
                                Log.d(
                                    LOG_TAG,
                                    "PurchaseState - Cancelled -> ${result.activePurchases}"
                                )
                            }
                        }
                    }
                }
            } else if (campaign.type == "label") {
                var label = campaign.label
                if (label == null) {
                    //
                    label = ""
                }
                NamiCampaignManager.launch(activity, label, paywallActionCallback = { action, skuId ->
                    Log.d(LOG_TAG, "New Paywall Action $action with ${skuId.orEmpty()}")
                }) { result ->
                    when (result) {
                        is LaunchCampaignResult.Success -> {
                            Log.d(LOG_TAG, "Launch Labeled Campaign Success - $label")
                        }
                        is LaunchCampaignResult.Failure -> {
                            Log.d(LOG_TAG, "Launch Labeled Campaign Error -> ${result.error}")
                            Toast.makeText(
                                activity,
                                "Campaign (label: $label) Launch Error -> ${result.error}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is LaunchCampaignResult.PurchaseChanged -> {
                            Log.d(LOG_TAG, "Purchase changed -> ${result.purchaseState}")

                            if (result.purchaseState == NamiPurchaseState.PURCHASED) {
                                Log.d(LOG_TAG, "Purchased! -> ${result.activePurchases}")
                            }
                            else if (result.purchaseState == NamiPurchaseState.CANCELLED) {
                                Log.d(
                                    LOG_TAG,
                                    "Cancelled Purchase Flow! -> ${result.activePurchases}"
                                )
                            }
                        }
                    }
                }
            }
        },
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = color
        )
    ) {
        Text(
            text = campaign.name,
            fontSize = 18.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CampaignsList() {
    CampaignCard(title = "Launch a Campaign", subtitle = "Tap a campaign to show a paywall")

    LazyColumn(modifier = Modifier.padding(top = 80.dp)) {
        items(campaignItems) { campaign ->
            CampaignRow(campaign)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CampaignsListTV() {
    CampaignCard(title = "Launch a Campaign", subtitle = "Choose a campaign to show a paywall")

    LazyVerticalGrid(
        modifier = Modifier.padding(top = 60.dp),
        columns = GridCells.Adaptive(200.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(campaignItems) { campaign ->
            CampaignRow(campaign)
        }
    }
}

@Composable
fun CampaignView(leanback: Boolean) {
    if (leanback) {
        CampaignsListTV()
    } else {
        CampaignsList()
    }
}

@Preview(showBackground = true)
@Composable
fun CampaignViewPreview() {
    CampaignView(leanback = false)
}
