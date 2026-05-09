package com.example.marketplacesiswa

import android.app.Application
import com.example.marketplacesiswa.data.MarketplaceDatabase

class MarketplaceSiswaApp : Application() {
    val database: MarketplaceDatabase by lazy { MarketplaceDatabase.getDatabase(this) }
}
