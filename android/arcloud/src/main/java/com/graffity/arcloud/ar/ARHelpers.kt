package com.graffity.arcloud.ar

import io.github.sceneview.node.Node

var Node.id: String?
    get() = name
    set(value) {
        name = value
    }