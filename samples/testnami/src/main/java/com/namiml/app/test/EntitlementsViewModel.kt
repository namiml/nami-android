package com.namiml.app.test

import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

class EntitlementsViewModel : ViewModel() {

    private val _activeEntitlementsStateFlow = MutableStateFlow<List<NamiEntitlement>>(
        NamiEntitlementManager.active()
    )
    val activeEntitlementsStateFlow: StateFlow<List<NamiEntitlement>> get() = _activeEntitlementsStateFlow

    init {
        NamiEntitlementManager.registerActiveEntitlementsHandler {
            _activeEntitlementsStateFlow.value = it
        }
    }
}