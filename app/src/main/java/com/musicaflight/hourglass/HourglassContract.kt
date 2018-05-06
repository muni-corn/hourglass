package com.musicaflight.hourglass

import android.provider.BaseColumns

/**
 * Created by harri_000 on 2/5/2017.
 */

 class HourglassContract {

    companion object HourglassColumns : BaseColumns {
        val _ID = BaseColumns._ID
        val TABLE_NAME = "hourglasses"
        val COLUMN_TIMESTAMP = "timestamp"
        val COLUMN_TITLE = "title"
        val COLUMN_SHOW_YEARS = "showyears"
        val COLUMN_SHOW_DAYS = "showdays"
        val COLUMN_SHOW_HOURS = "showhours"
        val COLUMN_SHOW_MINUTES = "showminutes"
        val COLUMN_SHOW_SECONDS = "showseconds"
        val COLUMN_DECIMAL_PLACES = "decimalplaces"
        val COLUMN_IMAGE_ID = "imageid"
    }

}
