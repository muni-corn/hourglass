package com.musicaflight.hourglass

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.musicaflight.hourglass.HourglassContract.HourglassColumns

/**
 * Created by harri_000 on 2/5/2017.
 */

internal class HourglassDatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

	override fun onCreate(db: SQLiteDatabase) = db.execSQL(SQL_CREATE_TABLE)

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		if (oldVersion <=2) {
			db.execSQL(SQL_DELETE_TABLE)
			onCreate(db)
		}
	}

	companion object {

		private val DATABASE_VERSION = 4

		private val DATABASE_NAME = "hourglasses.db"

		val SQL_DELETE_TABLE = "DELETE TABLE " + HourglassColumns.TABLE_NAME;

		val SQL_CREATE_TABLE = "CREATE TABLE " + HourglassColumns.TABLE_NAME + " (" +
				HourglassColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				HourglassColumns.COLUMN_TITLE + " TEXT NOT NULL, " +
				HourglassColumns.COLUMN_TIMESTAMP + " BIGINT NOT NULL, " +
				HourglassColumns.COLUMN_SHOW_YEARS + " BOOLEAN, " +
				HourglassColumns.COLUMN_SHOW_DAYS + " BOOLEAN, " +
				HourglassColumns.COLUMN_SHOW_HOURS + " BOOLEAN, " +
				HourglassColumns.COLUMN_SHOW_MINUTES + " BOOLEAN, " +
				HourglassColumns.COLUMN_SHOW_SECONDS + " BOOLEAN, " +
				HourglassColumns.COLUMN_DECIMAL_PLACES + " INTEGER, " +
				HourglassColumns.COLUMN_IMAGE_ID + " TEXT);"

		private var instance: HourglassDatabaseHelper? = null

		fun getInstance(context: Context): HourglassDatabaseHelper {
			if (instance == null) instance = HourglassDatabaseHelper(context)
			return instance!!
		}
	}
}
