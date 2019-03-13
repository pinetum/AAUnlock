package tw.qtlin.aa.aaunlock


import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class AppListAdapter(private val apps: List<ApplicationInfo>, private val ctx: MainActivity) :
    RecyclerView.Adapter<AppListAdapter.MyViewHolder>() {
    class MyViewHolder(val appItem: ConstraintLayout) : RecyclerView.ViewHolder(appItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val appItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item, parent, false) as ConstraintLayout
        appItem.setOnClickListener {

            ctx.findViewById<EditText>(R.id.textPackage).setText(it.findViewById<TextView>(R.id.appPackageName).text)
        }
        return MyViewHolder(appItem)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, i: Int) {
        val appInfo = apps[i]
        holder.appItem.findViewById<TextView>(R.id.appTitle).text = appInfo.loadLabel(ctx.packageManager).toString()
        holder.appItem.findViewById<TextView>(R.id.appPackageName).text = appInfo.packageName
        holder.appItem.findViewById<ImageView>(R.id.appIcon).setImageDrawable(appInfo.loadIcon(ctx.packageManager))
    }

}
