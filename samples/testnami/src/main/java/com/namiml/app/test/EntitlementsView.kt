package com.namiml.app.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager

@Composable
fun EntitlementsStatusText(status: String) {
    Text(
        text = status,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp),
        color = Color.Black
    )
}

@Composable
fun EntitlementsCard(status: String) {
    BoxWithConstraints {
        if (maxWidth < 400.dp) {
            Column {
                EntitlementsStatusText(status = status)
            }
        } else {
            Row {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EntitlementsStatusText(status = status)
                }
            }
        }
    }
}

@Composable
fun ActiveEntitlementRow(entitlement: NamiEntitlement) {
    Text(
        text = entitlement.referenceId,
        fontSize = 18.sp,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        color = Color.Black
    )
}

@Composable
fun EntitlementsView(
    leanback: Boolean = false,
    entitlementsViewModel: EntitlementsViewModel = viewModel()
) {
    val activeEntitlements by entitlementsViewModel.activeEntitlementsStateFlow.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Refresh") },
                onClick = {
                    NamiEntitlementManager.refresh() {
                        activeEntitlements.takeIf { it.size > 0 }
                    }
                },
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.background
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.padding(start = 40.dp, end = 40.dp).takeIf { leanback } ?: Modifier.padding(
                    0.dp
                )
            ) {
                item {
                    EntitlementsCard(
                        "No Active Entitlements for User".takeIf { activeEntitlements.count() == 0 } ?: "Active Entitlements"
                    )
                }
                if (activeEntitlements.count() != 0) {
                    item {
                        RowHeader("Active Entitlements")
                    }
                    items(activeEntitlements) { entitlement ->
                        ActiveEntitlementRow(entitlement)
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EntitlementsViewPreview() {
    EntitlementsView(leanback = false)
}
