package au.com.CarDVR.Roadvision.Viewer ;

import au.com.CarDVR.Roadvision.CustomDialog;
import au.com.CarDVR.Roadvision.R ;
import au.com.CarDVR.Roadvision.SimpleDialog ;

import android.app.AlertDialog ;
import android.content.Context ;
import android.content.DialogInterface ;

public class MediaUrlDialog extends SimpleDialog {

	public interface MediaUrlDialogHandler {
		void onCancel() ;
	}

	public MediaUrlDialog(Context context, String mediaUrl,
			MediaUrlDialogHandler handler) {

		super(context) ;
	}

	public void show() {

		CustomDialog alertDialog = new CustomDialog.Builder(mContext)
		.setTitle(mContext.getResources().getString(R.string.label_net_disconnected))
		.setMessage("Encounter an error with the video file")
		.setCancelable(false)
		.setNegativeButton(mContext.getResources().getString(R.string.label_Close),new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).create();
		alertDialog.show() ;
		
		
		
		
		
		
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext) ;
		builder.setTitle(mContext.getResources().getString(R.string.label_net_disconnected)) ;
		builder.setCancelable(false) ;
		builder.setInverseBackgroundForced(true) ;
		builder.setNegativeButton(mContext.getResources().getString(R.string.label_Close),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss() ;
					}
				}) ;

		AlertDialog alert = builder.create() ;
		alert.show() ;
	}
}
