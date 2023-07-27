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
import androidx.compose.material.*
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
import com.namiml.campaign.LaunchCampaignResult
import com.namiml.campaign.NamiCampaign
import com.namiml.campaign.NamiCampaignManager
import com.namiml.campaign.NamiCampaignType

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
    CampaignHeader(1, "Live Unlabeled Campaign"),
    CampaignHeader(2, "Live Labeled Campaign")
)

fun getHeader(headerGroup: Number): CampaignHeader {
    return headers.first { it.group.equals(headerGroup) }
}

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
                NamiCampaignManager.launch(activity, paywallActionCallback = { paywallEvent ->
                    Log.d(
                        LOG_TAG,
                        "${paywallEvent.action}"
                    )

                    Log.d(
                        LOG_TAG,
                        "\tcampaignId ${paywallEvent.campaignId}\n" +
                            "\tcampaignName ${paywallEvent.campaignName}\n" +
                            "\tcampaignType ${paywallEvent.campaignType}\n" +
                            "\tcampaignLabel ${paywallEvent.campaignLabel}\n" +
                            "\tcampaignUrl ${paywallEvent.campaignUrl}\n" +
                            "\tpaywallId ${paywallEvent.paywallId}\n" +
                            "\tpaywallName ${paywallEvent.paywallName}\n" +
                            "\tsegmentId ${paywallEvent.segmentId}\n" +
                            "\texternalSegmentId ${paywallEvent.externalSegmentId}\n" +
                            "\tdeepLinkUrl ${paywallEvent.deeplinkUrl}\n" +
                            "\tselectedItemId ${paywallEvent.componentChange?.id}\n" +
                            "\tselectedItemName ${paywallEvent.componentChange?.name}\n" +
                            "\tsku ${paywallEvent.sku?.skuId}\n" +
                            "\tpurchaseError ${paywallEvent.purchaseError}\n" +
                            "\tpurchaseError ${paywallEvent.purchases}\n"

                    )
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
                    }
                }
            } else if (campaign.type == "label") {
                var label = campaign.label
                if (label == null) {
                    //
                    label = ""
                }
                NamiCampaignManager.launch(activity, label, paywallActionCallback = { paywallEvent ->

                    Log.d(
                        LOG_TAG,
                        "${paywallEvent.action}"
                    )

                    Log.d(
                        LOG_TAG,
                        "\tcampaignId ${paywallEvent.campaignId}\n" +
                            "\tcampaignName ${paywallEvent.campaignName}\n" +
                            "\tcampaignType ${paywallEvent.campaignType}\n" +
                            "\tcampaignLabel ${paywallEvent.campaignLabel}\n" +
                            "\tcampaignUrl ${paywallEvent.campaignUrl}\n" +
                            "\tpaywallId ${paywallEvent.paywallId}\n" +
                            "\tpaywallName ${paywallEvent.paywallName}\n" +
                            "\tsegmentId ${paywallEvent.segmentId}\n" +
                            "\texternalSegmentId ${paywallEvent.externalSegmentId}\n" +
                            "\tdeepLinkUrl ${paywallEvent.deeplinkUrl}\n" +
                            "\tselectedItemId ${paywallEvent.componentChange?.id}\n" +
                            "\tselectedItemName ${paywallEvent.componentChange?.name}\n" +
                            "\tsku ${paywallEvent.sku?.skuId}\n" +
                            "\tpurchaseError ${paywallEvent.purchaseError}\n" +
                            "\tpurchaseError ${paywallEvent.purchases}\n"

                    )
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
fun CampaignsList(campaignItems: List<CampaignItem>) {
    CampaignCard(title = "Launch a Campaign", subtitle = "Tap a campaign to show a paywall")

    LazyColumn(modifier = Modifier.padding(top = 80.dp)) {
        items(campaignItems) { campaign ->
            CampaignRow(campaign)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CampaignsListTV(
    campaignItems: List<CampaignItem>
) {
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
fun CampaignView(
    leanback: Boolean,
    campaigns: List<NamiCampaign>
) {
    val campaignItems = campaigns.sortedBy { it.value }
        .filter { it.type == NamiCampaignType.LABEL }
        .map {
            CampaignItem(
                when (it.type) {
                    NamiCampaignType.DEFAULT -> 1
                    NamiCampaignType.LABEL -> 2
                    NamiCampaignType.URL -> 3
                },
                it.value ?: "default",
                when (it.type) {
                    NamiCampaignType.DEFAULT -> "default"
                    NamiCampaignType.LABEL -> "label"
                    NamiCampaignType.URL -> "url"
                },
                it.value
            )
        }

    if (leanback) {
        CampaignsListTV(campaignItems)
    } else {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Refresh") },
                    onClick = {
                        NamiCampaignManager.refresh() {
                            campaignItems.takeIf { it.size > 0 }
                        }
                    },
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.background
                )
            },
            content = {
                CampaignsList(campaignItems)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CampaignViewPreview() {
    CampaignView(leanback = false, listOf())
}