package tw.qtlin.aa.aaunlock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import eu.chainfire.libsuperuser.Shell
import kotlinx.android.synthetic.*
import android.text.TextUtils
import android.database.sqlite.SQLiteDatabase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import android.content.pm.ResolveInfo
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager


class MainActivity : AppCompatActivity() {

    private lateinit var btnAdd:Button
    private lateinit var textPackage:EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnAdd = findViewById(R.id.btnAdd)
        textPackage = findViewById(R.id.textPackage)

        btnAdd.setOnClickListener {
            unlock(textPackage.text.toString())
        }
        val appsList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        var appsAAList: ArrayList<ApplicationInfo> = ArrayList<ApplicationInfo>()

        for(app in appsList){
            if(app.metaData != null){
                if(app.metaData.get("com.google.android.gms.car.application") != null){
                    appsAAList.add(app)
                }
            }


        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = AppListAdapter(appsAAList, this)

        recyclerView = findViewById<RecyclerView>(R.id.appListView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }


    }

    fun unlock(packageName:String) {
        if(!Shell.SU.available()){
            Toast.makeText(this, "Can not grant su permission.", Toast.LENGTH_LONG)
            return
        }
        Shell.SU.run("pm disable --user 0 com.google.android.gms/.phenotype.service.sync.PhenotypeConfigurator")
        Shell.SU.run("pm disable --user 0 com.google.android.gms/.phenotype.service.PhenotypeService")
        Shell.SU.run("chmod 777 /data/data/com.google.android.gms/databases/phenotype.db*")
        try {
            val sql = SQLiteDatabase.openDatabase("/data/data/com.google.android.gms/databases/phenotype.db", null, 0)
            if (sql != null) {
                val cursor = sql.rawQuery(
                    "SELECT stringVal FROM Flags WHERE packageName=? AND name=?;",
                    arrayOf("com.google.android.gms.car", "app_white_list")
                )
                val packageNames = HashSet<String>()
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val stringVal = cursor.getString(cursor.getColumnIndex("stringVal"))
                        if (stringVal != null) {
                            packageNames.add(stringVal)
                        }
                    }
                }

                cursor.close()
                packageNames.add(packageName)
                sql.execSQL("DELETE FROM Flags WHERE packageName=\"com.google.android.gms.car\" AND name=\"app_white_list\";")
                val joinedPackageNames = TextUtils.join(",", packageNames)
                sql.execSQL("INSERT INTO Flags (packageName, version, flagType, partitionId, user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", 209, 0, 0, \"\", \"app_white_list\", \"$joinedPackageNames\", 1);")
                sql.execSQL("INSERT INTO Flags (packageName, version, flagType, partitionId, user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", 224, 0, 0, \"\", \"app_white_list\", \"$joinedPackageNames\", 1);")
                sql.close()
                Toast.makeText(
                    this,
                    "Successfully unlocked. Reboot phone and connect to Android Auto",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("sql exception : ")
            stringBuilder.append(e.toString())
            Toast.makeText(this, "Error in executing commands : $stringBuilder", Toast.LENGTH_LONG).show()
        }

        Shell.SU.run("chmod 660 /data/data/com.google.android.gms/databases/phenotype.db*")
        return

    }
}
