package app.simple.inure.adapters.menus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterHomeMenu(private val list: List<Pair<Int, Int>>) : RecyclerView.Adapter<AdapterHomeMenu.Holder>() {

    private lateinit var adapterHomeMenuCallbacks: AdapterHomeMenuCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_menu, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = holder.itemView.context.getString(list[position].second)
        holder.icon.setImageResource(list[position].first)
        holder.text.text = holder.itemView.context.getString(list[position].second)
        holder.container.setOnClickListener {
            adapterHomeMenuCallbacks.onMenuItemClicked(list[position].second, holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.adapter_app_info_menu_icon)
        val text: TypeFaceTextView = itemView.findViewById(R.id.adapter_app_info_menu_text)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.adapter_app_info_menu_container)

        init {
            text.isSelected = true
        }
    }

    fun setOnAppInfoMenuCallback(adapterHomeMenuCallbacks: AdapterHomeMenuCallbacks) {
        this.adapterHomeMenuCallbacks = adapterHomeMenuCallbacks
    }

    interface AdapterHomeMenuCallbacks {
        fun onMenuItemClicked(source: Int, icon: ImageView)
    }
}