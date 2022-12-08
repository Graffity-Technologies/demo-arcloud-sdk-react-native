package com.graffity.arcloud.sdkservice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class AnchorObjectViewModelFactory(
    private val accessToken: AccessToken,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ARContentViewModel::class.java)) {
            return ARContentViewModel(accessToken) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

internal class AccessTokenViewModelFactory(
    private val inputAccessToken: String,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccessTokenViewModel::class.java)) {
            return AccessTokenViewModel(inputAccessToken) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}