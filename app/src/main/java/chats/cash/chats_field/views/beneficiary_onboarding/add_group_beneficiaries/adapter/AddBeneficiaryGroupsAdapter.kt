package chats.cash.chats_field.views.beneficiary_onboarding.add_group_beneficiaries.adapter

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemBeneficiaryGroupMemberBinding
import chats.cash.chats_field.utils.convertDateToString
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.ui.getDateUsingPickerAsync
import chats.cash.chats_field.views.auth.ui.loadGlide
import chats.cash.chats_field.views.auth.viewmodel.BeneficiaryMembers
import chats.cash.chats_field.views.core.showErrorSnackbar
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.Calendar

class AddBeneficiaryGroupsAdapter(
    private val root: View,
    private val lifecycleCoroutineScope: LifecycleCoroutineScope,
    private val context: Context,
    val onChange: (Int) -> Unit,
    val onEditItem: (Int, BeneficiaryMembers) -> Unit,
    val onDone: (Int, BeneficiaryMembers) -> Unit,
    val onDeleteItem: (Int) -> Unit,
    private val selectImage: (BeneficiaryMembers, Int, (File) -> Unit) -> Unit,
) : RecyclerView.Adapter<AddBeneficiaryGroupsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemBeneficiaryGroupMemberBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_beneficiary_group_member, parent, false),
            ),
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    val tempDobMapping = mutableMapOf<Int, String>()
    val tempPicturesMapping = mutableMapOf<Int, File>()

    inner class MyViewHolder(private val binding: ItemBeneficiaryGroupMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BeneficiaryMembers) {
            binding.memberId.text =
                context.getString(
                    R.string.member_id_001,
                    (bindingAdapterPosition + 1).toString(),
                )
            binding.edit.setOnClickListener {
                binding.iseditingGroup.show()
                binding.edit.hide()
                binding.viewModeLayout.hide()
                onEditItem(bindingAdapterPosition, item.copy(isedit = true))
            }

            binding.done.setOnClickListener {
                val name = binding.fullname.text.toString()
                Timber.v(name)
                if (name.isEmpty() || name.length < 3 && item.name.isEmpty()) {
                    binding.fullNameParent.isErrorEnabled = true
                    binding.fullNameParent.error =
                        context.getString(R.string.enter_a_valid_name)
                } else if (tempDobMapping[bindingAdapterPosition].isNullOrEmpty() && item.dob.isNullOrEmpty()) {
                    binding.dateofbirth.error =
                        context.getString(R.string.enter_a_valid_date_of_birth)
                } else if (tempPicturesMapping[bindingAdapterPosition] == null && item.picture == null) {
                    showErrorSnackbar(
                        (R.string.upload_a_clear_picture_showing_your_face_navoid_group_picture),
                        root,
                    )
                } else {
                    Timber.v(name + tempDobMapping[bindingAdapterPosition])
                    binding.iseditingGroup.hide()
                    binding.edit.show()
                    binding.viewModeLayout.show()
                    onDone(
                        bindingAdapterPosition,
                        item.copy(
                            isedit = false,
                            name = binding.fullname.text.toString(),
                            dob = tempDobMapping[bindingAdapterPosition] ?: "",
                            picture = tempPicturesMapping[bindingAdapterPosition],
                        ),
                    )
                }
            }

            binding.delete.setOnClickListener {
                onDeleteItem(bindingAdapterPosition)
            }

            Timber.v(item.toString())
            item.apply {
                binding.dateofbirth.text = this.dob
                binding.fullname.text = Editable.Factory.getInstance().newEditable(this.name)

                binding.uploadPhoto.text = this.picture?.name
                binding.fullname.addTextChangedListener {
                    if (it?.toString() != null) {
                        onEditItem(bindingAdapterPosition, item.copy(name = it.toString()))
                        binding.noEditName.text = it.toString()
                    }
                }
            }
            binding.uploadPhoto.setOnClickListener {
                selectImage(item, bindingAdapterPosition) { file ->
                    binding.uploadPhoto.text = file.name
                    binding.profilePic.loadGlide(file)
                    tempPicturesMapping[bindingAdapterPosition] = file
                    onEditItem(bindingAdapterPosition, item.copy(picture = file))
                }
            }

            binding.dateofbirth.setOnClickListener {
                lifecycleCoroutineScope.launch {
                    val initialDate = Calendar.getInstance()
                    initialDate[Calendar.YEAR] = 2001
                    val data =
                        getDateUsingPickerAsync(context, initialDate)?.convertDateToString()
                            ?: ""
                    binding.dateofbirth.text = data
                    binding.noEitDob.text = data
                    tempDobMapping[bindingAdapterPosition] = data
                    onEditItem(bindingAdapterPosition, item.copy(dob = data))
                }
            }
            binding.noEditName.text = item.name
            binding.noEitDob.text = item.dob
            item.picture?.let { binding.profilePic.loadGlide(it) }
            if (item.isedit) {
                binding.iseditingGroup.show()
                binding.edit.show()
                binding.viewModeLayout.hide()
            } else {
                binding.iseditingGroup.hide()
                binding.edit.show()
                binding.viewModeLayout.show()
            }
        }
    }

    private var items = listOf<BeneficiaryMembers>()

    fun updateData(newItems: List<BeneficiaryMembers>) {
        items = newItems.toMutableList()
        val diffUtil = DiffUtilCallback(items, newItems)
        val res = DiffUtil.calculateDiff(diffUtil)
        res.dispatchUpdatesTo(this)
    }

    fun addItem(item: BeneficiaryMembers) {
        notifyItemInserted(items.indexOf(item))
        onChange(items.size)
    }

    fun removeItem(position: Int) {
        if (position != -1) {
            notifyItemRemoved(position)
        }
        onChange(items.size)
    }

    fun updateItem(item: BeneficiaryMembers, position: Int) {
        notifyItemChanged(position)
        onChange(items.size)
    }

    fun getItems(): List<BeneficiaryMembers> {
        return items
    }

    inner class DiffUtilCallback(
        private val oldList: List<BeneficiaryMembers>,
        private val newList: List<BeneficiaryMembers>,
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition] ||
                oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].name == newList[newItemPosition].name
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }
    }
}
