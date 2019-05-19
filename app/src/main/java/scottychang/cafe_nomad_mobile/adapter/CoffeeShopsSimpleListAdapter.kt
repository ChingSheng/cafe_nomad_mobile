package scottychang.cafe_nomad_mobile.adapter

import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.model.CoffeeShop


class CoffeeShopsSimpleListAdapter(
    private val title: String,
    private val data: List<Pair<CoffeeShop, Double>>?,
    private val onItemClick: (position: Int) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TITLE_TYPE = 0
    private val ITEM_TYPE = 1

    override fun getItemCount(): Int {
        val x = data?.size?.plus(1) ?: 1
        return x
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TITLE_TYPE else ITEM_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TITLE_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_coffee_simple_title, parent, false)
            CoffeeShopTitleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_coffee_simple_item, parent, false)
            CoffeeShopViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = if (position == TITLE_TYPE) {
        holder as CoffeeShopTitleViewHolder
        holder.onBind(title)
    } else {
        holder as CoffeeShopViewHolder
        holder.itemView.setOnClickListener { onItemClick.invoke(position - 1) }
        holder.onBind(data?.get(position - 1)!!)
    }

    class CoffeeShopViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun onBind(coffeeShop: Pair<CoffeeShop, Double>) {
            itemView.findViewById<TextView>(R.id.shop_name).text = coffeeShop.first.name + " " + getDistance(coffeeShop.second)
        }

        private fun getDistance(second: Double): String {
            if (second < 1000) {
                return "(" + second.toInt().toString() + "m)"
            } else {
                return "(" + ((second * 10).toInt().toDouble() / 10000).toString() + "km)"
            }
        }
    }

    class CoffeeShopTitleViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun onBind(title: String) {
            itemView.findViewById<TextView>(R.id.title).text = title;
        }
    }

    fun getBottomSheetCallback():BottomSheetBehavior.BottomSheetCallback {
        return object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheet as RecyclerView
                val imageView = bottomSheet.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.swipe_icon) as ImageView
                when(newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> imageView.setImageResource(R.drawable.bottom_sheet_slide_down)
                    BottomSheetBehavior.STATE_COLLAPSED -> imageView.setImageResource(R.drawable.bottom_sheet_slide_up)
                }
            }
        }

    }
}