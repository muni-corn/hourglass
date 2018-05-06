package com.musicaflight.hourglass

import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.END
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*


/**
 * Created by harri_000 on 8/17/2017.
 */

class HourglassListAdapter(private val context: Context) : RecyclerView.Adapter<HourglassListAdapter.ViewHolder>(), Content.ContentChangeListener {

	init {
		Content.addContentChangeListener(this)
	}

	private val TYPE_HOURGLASS = 0
	private val TYPE_SUBHEADER = 1
	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		ItemTouchHelper(object : ItemTouchHelper.Callback() {
			override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder): Int = if (viewHolder.itemViewType == TYPE_SUBHEADER) makeMovementFlags(0, 0) else makeMovementFlags(0, END)

			override fun isItemViewSwipeEnabled(): Boolean = true

			override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
				if (viewHolder is HourglassViewHolder) Content.deleteHourglass(viewHolder.hourglass)
			}

			override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
				(viewHolder as? HourglassViewHolder)?.pickUp()
				Log.d(javaClass.name, "onSelectedChanged")
				super.onSelectedChanged(viewHolder, actionState)
			}

			override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
				(viewHolder as? HourglassViewHolder)?.setDown()
				Log.d(javaClass.name, "clearView")
				super.clearView(recyclerView, viewHolder)
			}

		}).attachToRecyclerView(recyclerView)
		super.onAttachedToRecyclerView(recyclerView)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourglassListAdapter.ViewHolder? {
		return when (viewType) {
			TYPE_HOURGLASS -> HourglassViewHolder(LayoutInflater.from(context).inflate(R.layout.listitem_hourglass, parent, false))
			TYPE_SUBHEADER -> SubheaderViewHolder(LayoutInflater.from(context).inflate(R.layout.subheader, parent, false))
			else -> null
		}
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (holder is HourglassViewHolder) {
			var list = Content.UPCOMING
			var i = position
			val usePast = i > list.size
			when {
				i > list.size -> i -= (list.size + 1)
				i == list.size -> i -= (list.size)
			}
			if (usePast) list = Content.PAST

			holder.hourglass = list[i]
			holder.title.text = holder.hourglass.title
			holder.time.text = holder.hourglass.getShortDescription(context)
		}
	}

	override fun getItemViewType(position: Int): Int {
		return when (position) {
			Content.UPCOMING.size -> TYPE_SUBHEADER
			else -> TYPE_HOURGLASS
		}
	}

	override fun getItemCount(): Int = Content.UPCOMING.size + Content.PAST.size + if (Content.PAST.size > 0) 1 else 0

	override fun onHourglassAdded(hourglass: Hourglass, toList: MutableList<Hourglass>, position: Int) {
		if (toList == Content.UPCOMING) {
			notifyItemInserted(position)
			return
		} else if (toList.size == 1) {
			notifyItemInserted(Content.UPCOMING.size)
		}
		notifyItemInserted(position + 1 + Content.UPCOMING.size)
	}

	override fun onHourglassRemoved(hourglass: Hourglass, fromList: MutableList<Hourglass>, position: Int) {
		if (fromList == Content.UPCOMING) {
			notifyItemRemoved(position)
			return
		} else {
			notifyItemRemoved(position + 1 + Content.UPCOMING.size)
			if (fromList.size == 0)
				notifyItemRemoved(Content.UPCOMING.size)
		}
	}

	override fun onHourglassMoved(hourglass: Hourglass, fromList: MutableList<Hourglass>, fromPosition: Int, toList: MutableList<Hourglass>, toPosition: Int) {
		var p1 = fromPosition
		var p2 = toPosition

		if (fromList == Content.PAST || toList == Content.PAST) {
			val add = Content.UPCOMING.size + if (fromList == Content.PAST && toList == Content.UPCOMING) 0 else 1
			if (fromList == Content.PAST) p1 += add
			if (toList == Content.PAST) p2 += add
		}
		if (p1 != p2) notifyItemMoved(p1, p2)
		notifyItemChanged(p2)
	}

	open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

	inner class HourglassViewHolder(itemView: View) : ViewHolder(itemView) {

		lateinit var hourglass: Hourglass
		val title: TextView = itemView.findViewById(R.id.hourglass_name)
		val time: TextView = itemView.findViewById(R.id.hourglass_time_glance)

		init {
			itemView.setOnClickListener {
				for (ohicl in onHourglassItemClickListeners) {
					ohicl.onHourglassItemClick(hourglass)
				}
			}

		}

		private var up: Boolean = false
		fun pickUp() {
			if (up) return
			val animator: ObjectAnimator = ObjectAnimator.ofFloat(itemView, "elevation", 6f)
			animator.duration = 150
			animator.start()
			up = true
		}

		fun setDown() {
			if (!up) return
			val animator: ObjectAnimator = ObjectAnimator.ofFloat(itemView, "elevation", 0f)
			animator.duration = 300
			animator.start()
			up = false
		}
	}

	inner class SubheaderViewHolder(itemView: View) : ViewHolder(itemView)

	private val onHourglassItemClickListeners = ArrayList<OnHourglassItemClickListener>()

	fun addOnHourglassItemClickListener(listener: OnHourglassItemClickListener) = onHourglassItemClickListeners.add(listener)


	interface OnHourglassItemClickListener {
		fun onHourglassItemClick(h: Hourglass)
	}
}
