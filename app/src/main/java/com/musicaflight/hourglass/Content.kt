package com.musicaflight.hourglass

import android.content.Context
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.util.*

/**
 * Created by harri_000 on 8/16/2017.
 */

object Content {
	fun getImagePathFromImageId(c: Context, id: String): String = ContextWrapper(c).filesDir.absolutePath + File.separator + id

	var UPCOMING: MutableList<Hourglass> = ArrayList()
	var PAST: MutableList<Hourglass> = ArrayList()

	private lateinit var dbHelper: HourglassDatabaseHelper
	private lateinit var db: SQLiteDatabase

	fun load(context: Context) {
		UPCOMING.clear()
		PAST.clear()
		dbHelper = HourglassDatabaseHelper.getInstance(context)
		db = dbHelper.writableDatabase
		val cursor = db.query(HourglassContract.HourglassColumns.TABLE_NAME, null, null, null, null, null, null)
		while (cursor.moveToNext()) {
			val title = cursor.getString(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_TITLE))

			val timestamp = cursor.getLong(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_TIMESTAMP))

			val newHourglass = Hourglass(title, timestamp)

			newHourglass.id = (cursor.getLong(cursor.getColumnIndex(HourglassContract.HourglassColumns._ID)))

			newHourglass.showYears = (cursor.getInt(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_SHOW_YEARS)) > 0)
			newHourglass.showDays = (cursor.getInt(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_SHOW_DAYS)) > 0)
			newHourglass.showHours = (cursor.getInt(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_SHOW_HOURS)) > 0)
			newHourglass.showMinutes = (cursor.getInt(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_SHOW_MINUTES)) > 0)
			newHourglass.showSeconds = (cursor.getInt(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_SHOW_SECONDS)) > 0)
			newHourglass.imageId = cursor.getString(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_IMAGE_ID))
			newHourglass.decimalPlaces = cursor.getInt(cursor.getColumnIndex(HourglassContract.HourglassColumns.COLUMN_DECIMAL_PLACES))

			addHourglass(newHourglass)
		}
		cursor.close()
	}

	fun save() {
		db.delete(HourglassContract.HourglassColumns.TABLE_NAME, null, null)
		for (h in UPCOMING) saveHourglassToDatabase(h)
		for (h in PAST) saveHourglassToDatabase(h)
	}

	private fun saveHourglassToDatabase(h: Hourglass) =db.insert(HourglassContract.HourglassColumns.TABLE_NAME, null, h.contentValues)


	fun close() {
		dbHelper.close()
		db.close()
	}

	fun addHourglass(h: Hourglass) {
		if (h.id == -1L) {
			val max: Long = Math.max(
					UPCOMING
							.map { it.id }
							.max()
							?: -1,
					PAST
							.map { it.id }
							.max()
							?: -1)
			h.id = max + 1
		}
		val (list, i) = insertHourglass(h)
		if (i < 0) return
		for (ccl in contentChangeListeners) {
			ccl.onHourglassAdded(h, list, i)
		}
	}

	private fun insertHourglass(h: Hourglass): Pair<MutableList<Hourglass>, Int> {
		val list = if (h.millisRemaining > 0) UPCOMING else PAST
		val listSize = list.size
		loop@ for (i in list.indices) {
			if (h > list[i]) continue@loop
			list.add(i, h)
			return Pair(list, i)
		}
		list.add(listSize, h)
		return Pair(list, listSize)
	}

	fun updateHourglass(h: Hourglass) {
		if (h.id == -1L) {
			throw IllegalArgumentException("This hourglass does not have an id.")
		} else {
			var list = UPCOMING
			var i = searchForHourglassInList(h, list)
			if (i < 0) {
				list = PAST
				i = searchForHourglassInList(h, list)
			}
			list.removeAt(i)
			val (list2, i2) = insertHourglass(h)
			for (ccl in contentChangeListeners) {
				ccl.onHourglassMoved(h, list, i, list2, i2)
			}
		}
	}

	private fun searchForHourglassInList(hourglass: Hourglass, list: MutableList<Hourglass>): Int = list.indices.firstOrNull { list[it].id == hourglass.id } ?: -1


	fun deleteHourglass(h: Hourglass) {
		val list = if (h.millisRemaining > 0) UPCOMING else PAST
		val i = searchForHourglassInList(h, list)
		list.removeAt(i)
		for (ccl in contentChangeListeners) {
			ccl.onHourglassRemoved(h, list, i)
		}
	}

	private val contentChangeListeners = ArrayList<ContentChangeListener>()

	fun addContentChangeListener(listener: ContentChangeListener) = contentChangeListeners.add(listener)

	interface ContentChangeListener {
		fun onHourglassAdded(hourglass: Hourglass, toList: MutableList<Hourglass>, position: Int)
		fun onHourglassRemoved(hourglass: Hourglass, fromList: MutableList<Hourglass>, position: Int)
		fun onHourglassMoved(hourglass: Hourglass, fromList: MutableList<Hourglass>, fromPosition: Int, toList: MutableList<Hourglass>, toPosition: Int)
	}
}
