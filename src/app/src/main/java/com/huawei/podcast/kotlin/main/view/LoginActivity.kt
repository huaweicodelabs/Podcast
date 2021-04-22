package com.huawei.podcast.kotlin.main.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton
import com.huawei.podcast.R
import com.huawei.podcast.kotlin.preference.SharedPreference
import com.huawei.podcast.kotlin.utils.Constants
import java.util.* // ktlint-disable no-wildcard-imports

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var login: HuaweiIdAuthButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_g_c_login)
        login = findViewById(R.id.huawei_authorize)
        login.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setEmail()
        val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(Constants.SCOPE_PROFILE))
        scopeList.add(Scope(Constants.SCOPE_EMAIL))
        huaweiIdAuthParamsHelper.setScopeList(scopeList)
        val authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams()
        val service = HuaweiIdAuthManager.getService(this, authParams)
        startActivityForResult(service.signInIntent, Constants.SIGNIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SIGNIN) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener { // onSuccess
                    val user = AGConnectAuth.getInstance().currentUser
                    SharedPreference.save(getString(R.string.username), user.displayName)
                    SharedPreference.save(getString(R.string.email), huaweiAccount.getEmail())
                    SharedPreference.save(getString(R.string.userid), user.uid)
                    setResult(resultCode, data)
                    finish()
                }
                    .addOnFailureListener {
                        // onFail
                    }
            }
        }
    }
}
