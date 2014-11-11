package com.example.meetsdkdemo;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.meetsdkdemo.FileBrowser.OnFileSelectListener;
import com.moxtra.sdk.MXAccountManager;
import com.moxtra.sdk.MXAccountManager.MXAccountUnlinkListener;
import com.moxtra.sdk.MXMeetManager;
import com.moxtra.sdk.MXMeetManager.OnJoinMeetListener;
import com.moxtra.sdk.MXMeetManager.OnStartMeetListener;
import com.moxtra.sdk.MXSDKConfig.MXProfileInfo;
import com.moxtra.sdk.MXSDKConfig.MXUserIdentityType;
import com.moxtra.sdk.MXSDKConfig.MXUserInfo;
import com.moxtra.sdk.MXSDKException.MeetIsInProgress;
import com.moxtra.sdk.MXSDKException.MeetIsNotStarted;
import com.moxtra.sdk.MXSDKException.Unauthorized;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainActivity";
    private ProgressDialog mProgressDlg;

	private MXAccountManager getAccountMgr() {
		return ((MyApplication) this.getApplication()).getAccountManager();
	}
	
	private String getNotificationToken(){
		return ((MyApplication) this.getApplication()).getNotificationId();
	}
	
	private MyApplication getApp(){
		return ((MyApplication) this.getApplication());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.btn_setup_user).setOnClickListener(this);
//		findViewById(R.id.btn_update_notification).setOnClickListener(this);
		findViewById(R.id.btn_update_profile).setOnClickListener(this);
		findViewById(R.id.btn_start_meet).setOnClickListener(this);
		findViewById(R.id.btn_join_meet).setOnClickListener(this);
//		findViewById(R.id.btn_change_meet_color).setOnClickListener(this);
//		findViewById(R.id.btn_invite).setOnClickListener(this);
//		findViewById(R.id.btn_unlink).setOnClickListener(this);
		Intent intent = getIntent();
		String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        MXMeetManager.getInstance().setOnEndMeetListener(new MXMeetManager.OnEndMeetListener() {
            @Override
            public void onMeetEnded(String meetId) {
                Log.d(TAG, "onMeetEnded(), meetId = " + meetId);
            }
        });
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_setup_user:
			onClickSetupUser();
			break;
//		case R.id.btn_update_notification:
//			this.onClickUpdateNotification();
//			break;
		case R.id.btn_update_profile:
			onClickUpdateProfile();
			break;
		case R.id.btn_start_meet:
            onClickSetupUser();
			break;
		case R.id.btn_join_meet:
			onClickJoinMeet();
			break;
//		case R.id.btn_change_meet_color:
//			this.onClickChangeMeetColor();
//			break;
//		case R.id.btn_invite:
//			this.onClickInviteToMeet();
//			break;
//		case R.id.btn_unlink:
//			onClickUnlink();
//			break;
		}
	}

	private void onClickSetupUser() {
        if (!getAccountMgr().isLinked()) {
            handleSetupUser("dddd@moxtra.com", "Moxtra",
                    "Moxtra", null);
        }else {
            onClickStartMeet();
        }
//		if (getAccountMgr() != null) {
//			if (getAccountMgr().isLinked()) {
//				UIDevice.showToastMessage(this,
//						"Account is already setup, do unlink firstly to switch account!");
//				return;
//			}
//			showSettingDialog(true,"", "", new OnSettingListener() {
//				@Override
//				public void onSettingDone(final String emailOrUniqueID,
//						final String firstName, final String lastName) {
//					FileBrowser fb = new FileBrowser(MainActivity.this);
//					fb.selectFile(new OnFileSelectListener() {
//						@Override
//						public void onFileSelected(String fileSelected) {
//
//						}
//					});
//				}
//			});
//		}
	}
		
	private void handleSetupUser(final String emailOrUniqueID,
			final String firstName, final String lastName, String fileSelected) {
		MXUserInfo user = new MXUserInfo(emailOrUniqueID, MXUserIdentityType.IdentityUniqueId);
		Bitmap bmpAvatar = null;
		if (fileSelected != null)
			bmpAvatar = BitmapFactory.decodeFile(fileSelected);
		MXProfileInfo profile = new MXProfileInfo(firstName, lastName,bmpAvatar);
		boolean bRet = getAccountMgr().setupUser(user, profile, getApp().getNotificationId(),
				new MXAccountManager.MXAccountLinkListener() {
					@Override
					public void onLinkAccountDone(boolean result) {
						if (mProgressDlg != null) {
							mProgressDlg.dismiss();
							mProgressDlg = null;
						}
						if (result) {
                            onClickStartMeet();
						} else
							showToast(MainActivity.this,"Fail to setup User Account");
					}
				});
		if (bRet) {
			showProgressDlg(getString(R.string.Logging_In));
		} else {
			showToast(this, "Fail to Login");
		}
	}

    interface OnSettingListener {
		void onSettingDone(String emailOrUniqueID, String firstName, String lastName);
	}

	private void showSettingDialog(final boolean bShowUniqueID,final String initFName,
			final String initLName, final OnSettingListener listener) {
		final EditText edtUniqueId = new EditText(this);
		edtUniqueId.setHint("Input Unique ID");
		edtUniqueId.setText("AnyUniqueIdInYourApp");
		
		final EditText edtFirstName = new EditText(this);
		edtFirstName.setHint("First Name");
		edtFirstName.setText(initFName);
		
		final EditText edtLastName = new EditText(this);
		edtLastName.setHint("Last Name");
		edtLastName.setText(initLName);
		
		LinearLayout llContent = new LinearLayout(this);
		llContent.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lps.setMargins(5, 10, 5, 10);
		if(bShowUniqueID)
			llContent.addView(edtUniqueId, lps);
		llContent.addView(edtFirstName, lps);
		llContent.addView(edtLastName, lps);
		new AlertDialog.Builder(this).setMessage("Account Setting")
				.setView(llContent)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						if (TextUtils.isEmpty(edtUniqueId.getText()))
//							showSettingDialog(bShowUniqueID,edtFirstName.getText().toString(), 
//									edtLastName.getText().toString(), listener);
//						else
						{
							if(listener != null){
								listener.onSettingDone(edtUniqueId.getText().toString(),
										edtFirstName.getText().toString(), edtLastName.getText().toString());
							}
						}
					}
				}).setNegativeButton("Cancel", null).show();
	}

	private void onClickUpdateProfile() {
		// Show Dialog
		showSettingDialog(false, "", "", new OnSettingListener() {
			@Override
			public void onSettingDone(String emailOrUniqueID,
					final String firstName, final String lastName) {
				FileBrowser fb = new FileBrowser(MainActivity.this);
				fb.selectFile(new OnFileSelectListener() {
					@Override
					public void onFileSelected(String fileSelected) {
						if (getAccountMgr() != null) {
							Bitmap bmpAvatar = null;
							if (fileSelected != null) {
								bmpAvatar = BitmapFactory
										.decodeFile(fileSelected);
							}

							MXProfileInfo profile = new MXProfileInfo(
									firstName, lastName, bmpAvatar);
							boolean bRet = getAccountMgr().updateUserProfile(profile);
							if(bRet == false){
								showToast(getApplicationContext(), "Fail to update profile");
							}
							else
								showToast(getApplicationContext(), "Update Profile Done");
						}
					}
				});
			}
		});
	}

	private void onClickStartMeet() {
		showMeetSettingDialog(true,"","",new OnMeetInfoListener(){
			public void onSettingDone(String meetTopicOrId, String guideMessage){
				if (MXMeetManager.getInstance()!= null) {
					try {
						MXMeetManager.getInstance().startMeet(meetTopicOrId, guideMessage, 
								new OnStartMeetListener(){
							@Override
							public void onStartMeetDone(String meetId, String meetUrl) {
								String meetInfo = String.format("Meet is started. meetId=%s meetUrl=%s", meetId,meetUrl);
								showToast(MainActivity.this, meetInfo);
							}
						});
					} catch (Unauthorized e) {
						e.printStackTrace();
						showToast(MainActivity.this, "Account isn't setup!");
					} catch (MeetIsInProgress e) {
						e.printStackTrace();
						showToast(MainActivity.this, "A Meet is in progress!");
					}
				}
			}
		});
	}

	private void onClickJoinMeet() {
		showMeetSettingDialog(false,"","",new OnMeetInfoListener(){
			public void onSettingDone(final String meetTopicOrId, String guideMessage){
				if (MXMeetManager.getInstance()!= null) {
					try {
						MXMeetManager.getInstance().joinMeet(meetTopicOrId, "", 
								new OnJoinMeetListener(){
							@Override
							public void onJoinMeetDone(String meetId, String meetUrl) {
								String meetInfo = String.format("Meet is joined.  meetId=%s meetUrl=%s", meetId,meetUrl);
								showToast(MainActivity.this, meetInfo);
							}

							@Override
							public void onJoinMeetFailed() {
								showToast(MainActivity.this, "Fail to Join Meet: meetId="+meetTopicOrId);
							}
						});
					} catch (Unauthorized e) {
						e.printStackTrace();
						showToast(MainActivity.this, "Account isn't setup!");
					} catch (MeetIsInProgress e) {
						e.printStackTrace();
						showToast(MainActivity.this, "A Meet is in progress!");
					}
				}
			}
		});
	}
	
	private void onClickUpdateNotification(){
		if(getAccountMgr() != null){
			if(TextUtils.isEmpty(getNotificationToken())){
				Log.d("MainActivity","onClickUpdateNotification, notification is null");
			}
			else{
				Log.d("MainActivity","onClickUpdateNotification, notification is gotten already!");
				getAccountMgr().updateNotificationToken(getNotificationToken());
			}
		}
	}
	
	private void onClickChangeMeetColor(){
		if (getAccountMgr() != null) {
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			int color = 0xffffff&(int)r.nextInt();
			color |= 0xff000000;
			MXMeetManager.getInstance().setMeetUIStyleWithColor(color);
		}
	}

	private void onClickUnlink() {
		if (getAccountMgr() != null) {
			boolean bRet = getAccountMgr().unlinkAccount(
					new MXAccountUnlinkListener() {

						@Override
						public void onUnlinkAccountDone(MXUserInfo arg0) {
							if (mProgressDlg != null) {
								mProgressDlg.dismiss();
								mProgressDlg = null;
							}
							showToast(MainActivity.this, "Unlinked");
							
						}
					});
			if (bRet) {
				showProgressDlg(getString(R.string.Sign_Out));
			}
		}
	}
	
    interface OnMeetInfoListener {
        void onSettingDone(String meetTopicOrId, String guideMessage);
    }
	
	private void showMeetSettingDialog(final boolean isHost, final String initTopic,
			final String initMsg,final OnMeetInfoListener listener) {
        final EditText etTopic = new EditText(this);
        if (isHost){
            etTopic.setHint("Input Topic");
        	etTopic.requestFocus();
        }
        else {
            etTopic.setHint("Input MeetID");
            etTopic.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            etTopic.requestFocus();
        }

        final EditText etGuideMessage = new EditText(this);
        etGuideMessage.setHint("Guide Message");
        etGuideMessage.setText("Welcome To Join My Meet");
        LinearLayout llContent = new LinearLayout(this);
        llContent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.setMargins(5, 10, 5, 10);
        llContent.addView(etTopic, lps);
        if (isHost)
            llContent.addView(etGuideMessage, lps);

        new AlertDialog.Builder(this).setMessage("Meet Setting")
                .setView(llContent)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String topic = etTopic.getText().toString();
                        if (isHost) {
                            String guideMsg = etGuideMessage.getText().toString();
                            listener.onSettingDone(topic,  guideMsg);
                        } else {
                            listener.onSettingDone(topic,  "");
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show().setOnShowListener(new OnShowListener(){
					@Override
					public void onShow(DialogInterface dialog) {
						etTopic.requestFocus();
					}
                });
    }
	
	private void onClickInviteToMeet(){
		final EditText etInvitee = new EditText(this);
		etInvitee.setHint("Please Input Email");
        final EditText etGuideMessage = new EditText(this);
        etGuideMessage.setHint("Invitation Message");
        etGuideMessage.setText("Welcome To Join My Meetfff");
        LinearLayout llContent = new LinearLayout(this);
        llContent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.setMargins(5, 10, 5, 10);
        llContent.addView(etInvitee, lps);
         llContent.addView(etGuideMessage, lps);
         
		new AlertDialog.Builder(this).setMessage("Input Invitee")
				.setView(llContent)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String invitee = etInvitee.getText().toString();
						String guideMsg = etGuideMessage.getText().toString();
						if(!TextUtils.isEmpty(invitee)){
							ArrayList<MXUserInfo> invitees = new ArrayList<MXUserInfo>();
							invitees.add(new MXUserInfo(invitee,MXUserIdentityType.IdentityUniqueId));
							try {
								MXMeetManager.getInstance().inviteParticipants(invitees, guideMsg);
							} catch (MeetIsNotStarted e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								showToast(MainActivity.this, "Fail to invite");
							}
						}
					}
				}).show();
	}
	
	public static void showToast(Context context, String message) {
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void showProgressDlg(CharSequence message) {
		if (mProgressDlg != null) {
			mProgressDlg.dismiss();
			mProgressDlg = null;
		}
		mProgressDlg = new ProgressDialog(this);
		mProgressDlg.setMessage(message);
		mProgressDlg.show();
	}
}

