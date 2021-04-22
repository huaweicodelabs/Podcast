package com.huawei.podcast.java.main.view;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.entity.common.CommonConstant;
import com.huawei.hms.support.api.entity.hwid.HwIDConstant;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;
import com.huawei.podcast.R;
import com.huawei.podcast.java.preference.SharedPreference;
import com.huawei.podcast.java.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private HuaweiIdAuthButton login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_g_c_login);
        login = findViewById(R.id.huawei_authorize);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setEmail();
        List<Scope> scopeList = new ArrayList<>();
        scopeList.add(new Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE));
        scopeList.add(new Scope(HwIDConstant.SCOPE.SCOPE_ACCOUNT_EMAIL));
        huaweiIdAuthParamsHelper.setScopeList(scopeList);
        HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams();
        HuaweiIdAuthService service = HuaweiIdAuthManager.getService(this, authParams);
        startActivityForResult(service.getSignInIntent(), Constants.SIGN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SIGN_CODE) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken);
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // onSuccess
                        AGConnectUser user =  AGConnectAuth.getInstance().getCurrentUser();
                        SharedPreference.save(getString(R.string.username), user.getDisplayName());
                        SharedPreference.save(getString(R.string.email), huaweiAccount.getEmail());
                        SharedPreference.save(getString(R.string.userid), user.getUid());
                        setResult(resultCode, data);
                        finish();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // onFail
                            }
                        });
            }
        }

    }
}
