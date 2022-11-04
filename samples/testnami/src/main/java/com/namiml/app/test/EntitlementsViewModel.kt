package com.namiml.app.test

import androidx.lifecycle.ViewModel
import com.namiml.entitlement.NamiEntitlement
import com.namiml.entitlement.NamiEntitlementManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
