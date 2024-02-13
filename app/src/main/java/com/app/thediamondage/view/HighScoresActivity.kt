package com.app.thediamondage.view
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.thediamondage.R
import com.app.thediamondage.databinding.ActivityHighScoresBinding
import com.app.thediamondage.model.Record

class HighScoresActivity : AppCompatActivity() {
    lateinit var binding: ActivityHighScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayUserRecords()
    }

    private fun displayUserRecords() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("Score", Context.MODE_PRIVATE)
        val recordsString = sharedPreferences.getString("recordsList", "")
        Log.d("Mainrecord", recordsString.toString())

        if (!recordsString.isNullOrEmpty()) {
            val recordsList = mutableListOf<Record>()

            val recordsArray = recordsString.split(";")
            for (recordEntry in recordsArray) {
                try {
                    val recordValues = recordEntry.trim().split(",")
                    if (recordValues.size == 2) {
                        val motionCount = recordValues[0].trim().toInt()
                        val time = recordValues[1].trim().toInt()
                        recordsList.add(Record(motionCount, time))
                    } else {
                        Log.e("DisplayRecords", "Invalid record format: $recordEntry")
                    }
                } catch (e: NumberFormatException) {
                    Log.e("DisplayRecords", "Invalid record format: $recordEntry")
                }
            }

            val adapter = RecordsAdapter(recordsList)
            binding.highScoresRecyclerView.adapter = adapter
            binding.highScoresRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.highScoresRecyclerView.visibility = View.VISIBLE
            binding.textView.visibility = View.GONE
        } else {
            binding.highScoresRecyclerView.visibility = View.GONE
            binding.textView.visibility = View.VISIBLE
        }
    }

    private class RecordsAdapter(private val recordsList: List<Record>) :
        RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

        class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val recordTextView: TextView = itemView.findViewById(R.id.recordTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_record, parent, false)
            return RecordViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
            val currentRecord = recordsList[position]
            val recordNumber = position + 1
            holder.recordTextView.text = "$recordNumber. Motion: ${currentRecord.motionCount}, Time: ${currentRecord.time}"
        }


        override fun getItemCount() = recordsList.size
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
    override fun onResume() {
        super.onResume()
        displayUserRecords()
    }
}
