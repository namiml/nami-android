package com.namiml.app.test

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.namiml.customer.AccountStateAction
import com.namiml.customer.CustomerJourneyState
import com.namiml.customer.NamiCustomerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class ProfileViewModel : ViewModel() {

    var deviceId = mutableStateOf(NamiCustomerManager.deviceId())

    private val _journeyStateFlow = MutableStateFlow<CustomerJourneyState>(
        CustomerJourneyState(
            formerSubscriber = false,
            inGracePeriod = false,
            inTrialPeriod = false,
            inIntroOfferPeriod = false,
        ),
    )
    val journeyStateFlow: StateFlow<CustomerJourneyState> get() = _journeyStateFlow

    private val _isLoggedInFlow = MutableStateFlow<Boolean>(NamiCustomerManager.isLoggedIn())
    val isLoggedInFlow: StateFlow<Boolean> get() = _isLoggedInFlow

    private val _externalIdFlow = MutableStateFlow<String?>(NamiCustomerManager.loggedInId())
    val externalIdFlow: StateFlow<String?> get() = _externalIdFlow

    init {
        NamiCustomerManager.registerJourneyStateHandler {
            _journeyStateFlow.value = it
        }

        NamiCustomerManager.registerAccountStateHandler { accountStateAction, success, error ->

            _isLoggedInFlow.value = NamiCustomerManager.isLoggedIn()
            _externalIdFlow.value = NamiCustomerManager.loggedInId()

            if (success) {
                if (accountStateAction == AccountStateAction.LOGIN) {
                    Log.d(LOG_TAG, "User is logged in")
                } else if (accountStateAction == AccountStateAction.LOGOUT) {
                    Log.d(LOG_TAG, "User is logged out")
                } else if (accountStateAction == AccountStateAction.ADVERTISING_ID_SET) {
                    Log.d(LOG_TAG, "Ad id was set")
                } else if (accountStateAction == AccountStateAction.ADVERTISING_ID_CLEARED) {
                    Log.d(LOG_TAG, "Ad id was cleared")
                } else if (accountStateAction == AccountStateAction.VENDOR_ID_SET) {
                    Log.d(LOG_TAG, "Vendor id was set")
                } else if (accountStateAction == AccountStateAction.VENDOR_ID_CLEARED) {
                    Log.d(LOG_TAG, "Vendor id was cleared")
                } else if (accountStateAction == AccountStateAction.CUSTOMER_DATA_PLATFORM_ID_SET) {
                    Log.d(LOG_TAG, "CDP id was set")
                } else if (accountStateAction == AccountStateAction.CUSTOMER_DATA_PLATFORM_ID_CLEARED) {
                    Log.d(LOG_TAG, "CDP id was cleared")
                }
            } else if (error != null) {
                if (accountStateAction == AccountStateAction.LOGIN) {
                    Log.d(LOG_TAG, "There was an error logging in. Error - $error")
                } else if (accountStateAction == AccountStateAction.LOGOUT) {
                    Log.d(LOG_TAG, "There was an error logging out. Error - $error")
                } else if (accountStateAction == AccountStateAction.ADVERTISING_ID_SET) {
                    Log.d(LOG_TAG, "There was an error setting the ad id. Error - $error")
                } else if (accountStateAction == AccountStateAction.ADVERTISING_ID_CLEARED) {
                    Log.d(LOG_TAG, "There was an error clearing the ad id. Error - $error")
                } else if (accountStateAction == AccountStateAction.VENDOR_ID_SET) {
                    Log.d(LOG_TAG, "There was an error setting the vendor id. Error - $error")
                } else if (accountStateAction == AccountStateAction.VENDOR_ID_CLEARED) {
                    Log.d(LOG_TAG, "There was an error clearing the ad id. Error - $error")
                } else if (accountStateAction == AccountStateAction.CUSTOMER_DATA_PLATFORM_ID_SET) {
                    Log.d(LOG_TAG, "There was an error setting the cdp id. Error - $error")
                } else if (accountStateAction == AccountStateAction.CUSTOMER_DATA_PLATFORM_ID_CLEARED) {
                    Log.d(LOG_TAG, "There was an error clearing the CDP id. Error - $error")
                }
            }
        }

        NamiCustomerManager.setCustomerDataPlatformId(withId = "A_CDP_ID")
        NamiCustomerManager.setVendorId(withId = UUID.randomUUID())
        NamiCustomerManager.setAdvertisingId(withId = UUID.randomUUID())

        NamiCustomerManager.clearCustomerDataPlatformId()
        NamiCustomerManager.clearVendorId()
        NamiCustomerManager.clearAdvertisingId()
    }
}
