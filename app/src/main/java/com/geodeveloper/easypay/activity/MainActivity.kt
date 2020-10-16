package com.geodeveloper.easypay.activity

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.geodeveloper.easypay.Constants
import com.geodeveloper.easypay.R
import com.geodeveloper.paybills.helper.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

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
                        .child(getString(R.string.account_balance)).getValue().toString()
                }

            }
            Utils.databaseRef().addValueEventListener(userListener)
        }
    }
}