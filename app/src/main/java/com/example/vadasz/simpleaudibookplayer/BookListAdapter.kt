package com.example.vadasz.simpleaudibookplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView

class ListExampleAdapter(context: Context) : BaseAdapter() {
    private var sList = arrayOf("One", "Two", "Three", "Four", "Five", "Six", "Seven",
            "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen")
    private val mInflator: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return sList.size
    }

    override fun getItem(position: Int): Any {
        return sList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return with(parent.context) {
            relativeLayout {
                textView(sList[position]) {
                    textSize = 32f
                }
                textView(sList[position]) {
                    textSize = 16f
                }.lparams {
                    alignParentBottom()
                    alignParentRight()
                }
            }
        }
    }
}
