package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.model.ModelCampaign
import com.codose.chats.offline.OfflineRepository
import com.codose.chats.utils.*
import com.codose.chats.views.auth.adapter.CashForWorkAdapter
import com.codose.chats.views.auth.adapter.CashForWorkClickListener
import kotlinx.android.synthetic.main.fragment_cash_for_work.*
import kotlinx.android.synthetic.main.layout_empty.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class CashForWorkFragment : Fragment() {
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()
    private lateinit var adapter: CashForWorkAdapter
    private lateinit var offlineRepository: OfflineRepository
    private var cashForWorksArray: ArrayList<ModelCampaign> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cashForWorkViewModel.getCashForWorks(PrefUtils.getNGOId().toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cash_for_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CashForWorkAdapter(CashForWorkClickListener {
            findNavController().navigate(CashForWorkFragmentDirections.actionCashForWorkFragmentToCashForWorkTaskFragment(
                cfwId = it.id.toString(),
                cfwName = it.title!!,
            ))
        })
        cfw_rv.adapter = adapter
        cfw_back_btn.setOnClickListener{
            findNavController().navigateUp()
        }
        cfw_progress.show()
        setObservers()
    }

    private fun setObservers() {
        cashForWorkViewModel.cashForWorkCampaign.observe(viewLifecycleOwner, {
            if (it != null){
                cfw_progress.hide()
                cashForWorksArray.clear()
                Timber.v("All campaigns "+it.toString())
                when (it.size) {
                    /*is ApiResponse.Failure -> {
                        cfw_progress.hide()
                        requireContext().toast(it.message)
                        findNavController().navigateUp()
                    }
                    is ApiResponse.Loading -> {
                        cfw_progress.show()
                    }
                    is ApiResponse.Success -> {

                        if(data.cashForWorks.isEmpty()){
                            cfw_empty.show()
                            cfw_empty.txt_not_found.text = "No cash for works"
                        }else{
                            cfw_empty.hide()
                        }
                    }*/
                    0 ->{
                        cfw_empty.show()
                        cfw_empty.txt_not_found.text = "No cash for works"
                    }
                    else ->{
                        var i = 0
                        for (cashForWork in it){
                            if (cashForWork.type.equals("cash-for-work") && cashForWork.status.equals("active")){
                                cashForWorksArray.add(cashForWork)
                            }
                        }
                        if (cashForWorksArray.isEmpty()){
                            cfw_empty.show()
                            cfw_empty.txt_not_found.text = "No cash for works"
                        }
                        else{
                            val data =
                                adapter.submitList(cashForWorksArray.toList())
                        }
                    }
                }
            }
        })
    }
}