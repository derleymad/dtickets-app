package com.derleymad.myapplication.ui.fragments.pager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.derleymad.myapplication.TicketActivity
import com.derleymad.myapplication.adapter.TicketsAdapter
import com.derleymad.myapplication.databinding.FragmentAbertosBinding
import com.derleymad.myapplication.model.Ticket
import com.derleymad.myapplication.utils.GetTicketsAbertosRequest
import com.google.android.material.snackbar.Snackbar


class AbertosFragment: Fragment() ,GetTicketsAbertosRequest.Callback{

    private lateinit var binding : FragmentAbertosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("testeview","oncreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAbertosBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreference =  view.context.getSharedPreferences("credentials", Context.MODE_PRIVATE)

        val username = sharedPreference.getString("username","none")?: throw  java.lang.IllegalStateException(
            "Não devia estar aqui sem ter feito login!"
        )
        val password = sharedPreference.getString("password","none")?: throw  java.lang.IllegalStateException(
            "Não devia estar aqui sem ter feito login!"
        )

        binding.swipeRefresh.setOnRefreshListener {
            binding.rvAbertos.visibility = View.INVISIBLE
            binding.swipeRefresh.isRefreshing = true
            GetTicketsAbertosRequest(this@AbertosFragment).execute(username,password)
        }
        GetTicketsAbertosRequest(this@AbertosFragment).execute(username,password)
    }

    override fun onPreExecute() {
        if(binding.swipeRefresh.isRefreshing){
            binding.included.errorContainer.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onResult(tickets: List<Ticket>) {
        binding.rvAbertos.visibility = View.VISIBLE
        binding.rvAbertos.adapter = TicketsAdapter(tickets) { it ->
            val intent = Intent(context, TicketActivity::class.java)
            intent.putExtra("id", it)
            startActivity(intent)
        }

        binding.rvAbertos.layoutManager = LinearLayoutManager(view?.context ?: null)
        binding.progressBar.visibility = View.INVISIBLE
        binding.rvAbertos.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = false

    }

    override fun onFailure(message: String) {
        binding.progressBar.visibility = View.INVISIBLE
        binding.included.errorContainer.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = false
        Snackbar.make(binding.root,"Sem conexão", Snackbar.LENGTH_SHORT).show()
        Log.e("internet_error_abertos",message)
    }

}