package com.example.chess_time

import android.content.Intent
import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    // Differentiates between the activity results of the add/edit timer class
    companion object {
        const val ADD_TIMER_REQUEST = 1
        const val EDIT_TIMER_REQUEST = 2
    }

    private lateinit var timerViewModel: TimerViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pass data back from add timer activity
        buttonAddTimer.setOnClickListener {
            startActivityForResult(
                Intent(this, AddEditTimerRequest::class.java),
                ADD_TIMER_REQUEST
            )
        }

        // Layout manager handles recycling and dimensions of the recycler view
        timer_card_recycler_view.layoutManager = LinearLayoutManager(this)
        timer_card_recycler_view.setHasFixedSize(true)

        // Adapter deals with binding and inflating actual data to recycler view cards
        var adapter = TimerAdapter()
        timer_card_recycler_view.adapter = adapter

        // Android will destroy the view when the activity is not needed anymore
        // Adds capability to persist orientation and life cycle changes, we're passing a class using livedata
        timerViewModel = ViewModelProviders.of(this).get(TimerViewModel::class.java)

        // This observer manages live data through life cycle changes and more
        timerViewModel.getAllTimers().observe(this, Observer<List<Timer>> {
            adapter.submitList(it)

        })

        // Assign adapter click listener from TimerAdapter
        adapter.setOnItemClickListener(object: TimerAdapter.OnItemClickListener {
            override fun onItemClick(timer: Timer) {
                // Store extras in intent
                var intent = Intent(baseContext, MatchActivity::class.java)
                intent.putExtra(MatchActivity.EXTRA_TIME, timer.durationWhite)
                intent.putExtra(MatchActivity.EXTRA_DELAY, timer.delayWhite)
                intent.putExtra(MatchActivity.EXTRA_INC , timer.incrementWhite)

                // Start Match activity and pass this intent
                startActivity(intent)
            }
        })

        // Creating new itemtouchhelper class with callback containing implemented methods and attaching to recycler view
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                timerViewModel.delete(adapter.getTimerAt(viewHolder.adapterPosition))
            }
        }).attachToRecyclerView(timer_card_recycler_view)

    }

    // Receives data from add/edit activity and differentiates the response
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_TIMER_REQUEST && resultCode == Activity.RESULT_OK) {
            val type = "Blitz"
            val playCount = 0

            val newTimer = Timer(
                // !! Ensures data is not null
                data!!.getStringExtra(AddEditTimerRequest.EXTRA_TITLE),
                data.getIntExtra(AddEditTimerRequest.EXTRA_TIME, 5),
                data.getIntExtra(AddEditTimerRequest.EXTRA_TIME, 5),
                data.getIntExtra(AddEditTimerRequest.EXTRA_INC, 5),
                data.getIntExtra(AddEditTimerRequest.EXTRA_INC, 5),
                data.getIntExtra(AddEditTimerRequest.EXTRA_DELAY, 5),
                data.getIntExtra(AddEditTimerRequest.EXTRA_DELAY, 5),
                type,
                playCount
            )

            timerViewModel.insert(newTimer)

            Toast.makeText(this, "Timer saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Timer abandoned", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}

