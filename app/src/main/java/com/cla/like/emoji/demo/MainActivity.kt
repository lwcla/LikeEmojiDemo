package com.cla.like.emoji.demo

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val adapter by lazy {
        val a = MyAdapter()
        rvData.layoutManager = LinearLayoutManager(this)
        rvData.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        rvData.adapter = a
        a
    }

    private val dataList = mutableListOf<LikeBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repeat(300) {
            dataList.add(LikeBean())
        }

        adapter.refreshData(dataList)
    }
}

private class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val dataList = mutableListOf<LikeBean>()

    private val random = Random()

    fun refreshData(list: List<LikeBean>) {
        if (dataList != list) {
            dataList.clear()
            dataList.addAll(list)
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val context = parent.context
        val g = random.nextInt(3)
        val contentView = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            isClickable = false
            isFocusable = false
            gravity = when (g) {
                2 -> {
                    Gravity.CENTER
                }
                0 -> {
                    Gravity.START
                }
                else -> {
                    Gravity.END
                }
            }
        }

        val pad = 50
        val likeView = LikeView(context).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100)
            params.marginEnd = pad
            params.marginStart = pad
            params.topMargin = pad
            params.bottomMargin = pad
            layoutParams = params

            isClickable = true
            isFocusable = true
            id = R.id.viewLike
        }
        contentView.addView(likeView)

        return MyViewHolder(contentView)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val likeView by lazy { itemView.findViewById<LikeView>(R.id.viewLike) }

        fun bind(bean: LikeBean) {
            likeView.bind(bean)
        }
    }
}

data class LikeBean(var like: Boolean = false)