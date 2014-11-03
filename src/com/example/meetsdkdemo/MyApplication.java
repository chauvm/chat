package com.example.meetsdkdemo;

import android.app.Application;
import android.util.Log;

import com.moxtra.binder.SDKConstant;
import com.moxtra.sdk.MXAccountManager;
import com.moxtra.sdk.MXSDKException.InvalidParameter;

public class MyApplication extends Application {
	static private final String clientId = "mGnCZfILvBo";
	static private final String clientSecret ="mQ9AlA2Vdso";

	private String gcmRegId;
	private MXAccountManager mAcctMgr;

	@Override
	public void onCreate(){
		super.onCreate();
		//this.registerGCM();
		initAccountManager();
	}
	
	private void initAccountManager(){
		if(mAcctMgr == null){
			try {
				String id = clientId;
				String secret = clientSecret;
				Log.d("MyApplication","initAccountManager id=" + id + " secret=" + secret);
				mAcctMgr = MXAccountManager.createInstance(this, id, secret);
			} catch (InvalidParameter e) {
				e.printStackTrace();
			}
		}
	}
	
	public MXAccountManager getAccountManager(){
		return mAcctMgr;
	}
	
    public void setGCMRegId(String regId){
        gcmRegId = regId;
        if(mAcctMgr != null){
            mAcctMgr.updateNotificationToken(gcmRegId);
        }
    }

    public String getNotificationId(){
        return gcmRegId;
    }

	@Override
	public void onTerminate(){
		super.onTerminate();
	}
}
