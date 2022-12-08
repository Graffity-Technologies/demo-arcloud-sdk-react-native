package com.graffity.arcloud.sdkservice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graffity.arcloud.ApiStatus
import kotlinx.coroutines.launch

internal class AccessTokenViewModel(private val inputAccessToken: String) : ViewModel() {
    private val tag = "AccessTokenViewModel"

    // private val userInputAccessToken = BuildConfig.GRAFFITY_SECRET_TOKEN

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<ApiStatus>()
    private val _data = MutableLiveData<AccessToken>()

    // The external immutable LiveData for the request status
    val status: LiveData<ApiStatus> = _status
    val data: LiveData<AccessToken> = _data

    init {
        Log.d(tag, "Init")

        validateAccessToken()
    }

    private fun validateAccessToken() {
//        Log.d(tag, "validateAccessToken")
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
//                Log.d(tag, "try")
//                Log.d(tag, inputAccessToken)
                val body = HashMap<String, String>()
                body["raw"] = inputAccessToken
                val response = SdkApi.retrofitService.validateAccessToken(
                    body
                    // ValidateAccessTokenBody(inputAccessToken)
                )
                if (response.isSuccessful) {
                    _data.postValue(response.body())
                    _status.value = ApiStatus.DONE
//                    Log.d(tag, response.body().toString())
                }
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                Log.e(tag, e.message.toString())
            }
        }
    }
}

internal class ARContentViewModel(private val accessToken: AccessToken) : ViewModel() {
    private val tag = "ARContentViewModel"

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<ApiStatus>()
    private val _data = MutableLiveData<List<ARContent>>()

    // The external immutable LiveData for the request status
    val status: LiveData<ApiStatus> = _status
    val data: LiveData<List<ARContent>> = _data

    init {
        getAnchorObjects()
    }

    private fun getAnchorObjects() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                val body = HashMap<String, String>()
                body["accessTokenRefId"] = accessToken._id
                val response = SdkApi.retrofitService.getAnchorObjects(
                    body
                    // GetAnchorObjectsBody(accessToken._id)
                )
                if (response.isSuccessful) {
                    _data.postValue(response.body())
                    _status.value = ApiStatus.DONE
//                    Log.d(tag, response.body().toString())
                }
            } catch (e: Exception) {
                _data.value = listOf()
                _status.value = ApiStatus.ERROR
                Log.e(tag, e.message.toString())
            }
        }
    }
}

//internal class SdkApiViewModel : ViewModel() {
//    private val tag = "SDKApiViewModel"
//
//    private val userInputAccessToken = BuildConfig.GRAFFITY_SECRET_TOKEN
//
//    private val _accessToken = MutableLiveData<AccessToken>()
//    val accessToken: LiveData<AccessToken> = _accessToken
//
//    private val _anchorObjects = MutableLiveData<List<AnchorObject>>()
//    val anchorObjects: LiveData<List<AnchorObject>> = _anchorObjects
//
//    init {
//        Log.d(tag, "Init")
//        validateAccessToken()
//    }
//
//    private fun validateAccessToken() {
//        Log.d(tag, "validateAccessToken")
//        viewModelScope.launch {
////            _status.value = ApiStatus.LOADING
//            try {
//                Log.d(tag, "try")
//                val response = SdkApi.retrofitService.validateAccessToken(ValidateAccessTokenBody(userInputAccessToken))
//                _accessToken.postValue(response.body())
////                _status.value = ApiStatus.DONE
//                Log.d(tag, response.body().toString())
//            } catch (e: Exception) {
////                _status.value = ApiStatus.ERROR
//                Log.e(tag, e.message.toString())
//            }
//        }
//    }
//}