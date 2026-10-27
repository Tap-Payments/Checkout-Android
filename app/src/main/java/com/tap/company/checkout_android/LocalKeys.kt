package com.tap.company.checkout_android

import com.google.gson.Gson

/**
 * Your own sandbox keys, kept out of the repo. Debug builds read keys.local.json at the repo root (it is
 * gitignored) when they are built, and Settings > Saved Keys switches between them. See keys.local.example.json
 * for the format; LocalKeys.swift does the same in the iOS example apps.
 */
data class LocalKey(val name: String, val publicKey: String, val merchantId: String? = null)

object LocalKeys {
    val all: List<LocalKey> by lazy {
        runCatching { Gson().fromJson(BuildConfig.LOCAL_KEYS_JSON, Array<LocalKey>::class.java).toList() }.getOrDefault(emptyList())
    }
}
