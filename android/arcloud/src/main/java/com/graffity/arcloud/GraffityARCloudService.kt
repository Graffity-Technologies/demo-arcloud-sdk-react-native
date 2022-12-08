package com.graffity.arcloud

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.graffity.arcloud.sdkservice.AccessTokenViewModel

class GraffityARCloudService: Fragment() {
    var accessToken: String? = null

    private val sharedAccessTokenViewModel: AccessTokenViewModel by activityViewModels()


}