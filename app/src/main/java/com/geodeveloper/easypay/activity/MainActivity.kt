package com.geodeveloper.easypay.activity

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flutterwave.raveandroid.RaveConstants
import com.flutterwave.raveandroid.RavePayActivity
import com.geodeveloper.easypay.Constants
import com.geodeveloper.easypay.R
import com.geodeveloper.paybills.helper.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    var amount : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        readAmountFromFireBase()
        btnFundWallet.setOnClickListener {
            val enterAmountFragment = FundWallet()
            enterAmountFragment.show(supportFragmentManager, "MainActivity")

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            val message: String = data.getStringExtra("response").toString()
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    val jsonObject = JSONObject(message)
                    val jsonData = jsonObject.getJSONObject("data")
                    val amountPaid = jsonData.getString("amount")
                    val amountPaidInDouble = amountPaid.toDouble()
                    getPreviousBalance(amountPaidInDouble)
                }
                RavePayActivity.RESULT_ERROR -> {
                    Toast.makeText(this, "ERROR $message", Toast.LENGTH_SHORT).show()
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    Toast.makeText(this, "CANCELLED $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }


    private fun getPreviousBalance(amount: Double) {
        val userListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                val newAmount = dataSnapShot.child(Constants.users)
                    .child(Utils.currentUserID())
                    .child(getString(R.string.account_balance)).value.toString().toDouble()
                Log.e("MainActivity", newAmount.toString())

                val newBalance : Double  = newAmount + amount
                Toast.makeText(this@MainActivity, "new Balance $newBalance", Toast.LENGTH_LONG).show()
                Log.e("MainActivity", newBalance.toString())
                updateAmount(newBalance)
            }

        }
        Utils.databaseRef().addListenerForSingleValueEvent(userListener)
    }

    private fun updateAmount(newBalance: Double) {
        Utils.databaseRef().child(Constants.users).child(Utils.currentUserID()).child(getString(R.string.account_balance)).setValue(newBalance)
            .addOnSuccessListener {
                Toast.makeText(this, "funded successfully", Toast.LENGTH_LONG).show()

            }
    }

    private fun readAmountFromFireBase() {
        if (Utils.currentUser() == null) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, OnBoardActivity::class.java)
            startActivity(intent)

        } else {
            val userListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }


                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    tvDisplayAmount.text = dataSnapShot.child(Constants.users)
                        .child(Utils.currentUserID())
                        .child(getString(R.string.account_balance)).value.toString()
                }

            }
            Utils.databaseRef().addValueEventListener(userListener)
        }
    }
}