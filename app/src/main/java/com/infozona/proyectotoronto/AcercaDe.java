package com.infozona.proyectotoronto;




import java.util.HashMap;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class AcercaDe extends AppCompatActivity {

    private static final String AD_UNIT_ID = "ca-app-pub-2671808875739238/7928133902";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acerca_de);
		
		//CARGAR PROPAGANDA
		CargarBannerAd();
		
		//ANALYTIC
		EnviarPantalla("AcercaDeActivity");
				
		ImageView imagen = (ImageView)findViewById(R.id.imageView1);
		
		imagen.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) 
			{
				String mensaje = "Cajeros UY\n\n";
				mensaje += "Gracias por todo el apoyo brindado\n\n";
				mensaje += "Desarrollador:\n";
				mensaje += "	Juan Camilo Volpe\n\n";
				mensaje += "Dise�o y Marketing:\n";
				mensaje += "	Oscar Olivera\n\n";
				mensaje += "Tester: \n";
				mensaje	+= " 	Guillermo Maestre";
				
						
				MostrarAcercaDe(mensaje, AcercaDe.this);
				return true;
			}
		});
		
		Button btnEnviarMail = (Button)findViewById(R.id.btnDanosTuOpinion);
		btnEnviarMail.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v) 
			{
				EnviarEventoAnalytics();
				Intent mail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:cajerosuy@infozona.org"));
				mail.putExtra(Intent.EXTRA_SUBJECT, "Opinion Cajeros UY");
				startActivity(Intent.createChooser(mail, "Elige un cliente de correo"));
				
			}
		});
		
		//Acerca de
		PackageInfo pInfo;
		TextView txtVersion = (TextView)findViewById(R.id.txtVersion);
		
		try 
		{
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			txtVersion.setText(pInfo.versionName);
		} 
		catch (NameNotFoundException e) 
		{

			e.printStackTrace();
		}
		
		
	}
	
	public enum TrackerName
	{
		APP_TRACKER,
		GLOBAL_TRACKER,
		ECOMMERCE_TRACKER,
	}
	HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
	
	synchronized Tracker getTracker(TrackerName trackerId)
	{
		if(!mTrackers.containsKey(trackerId))
		{
			
			GoogleAnalytics analytic = GoogleAnalytics.getInstance(this);
			//analytic.setDryRun(true);
			
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytic.newTracker("UA-60369825-1")
					: (trackerId == TrackerName.GLOBAL_TRACKER)? analytic.newTracker(R.xml.global_tracker)
							: analytic.newTracker("UA-60369825-1");
					
			mTrackers.put(trackerId, t);
			
		}
		
		return mTrackers.get(trackerId);
	}
	
	protected void EnviarPantalla(String nombrePantalla)
	{
		//CARGAR ANALYTICS
		Tracker t = getTracker(TrackerName.GLOBAL_TRACKER);
		t.setScreenName(nombrePantalla);
		t.send(new HitBuilders.AppViewBuilder().build());
		
	}
	protected void EnviarEventoAnalytics()
	{
		//CARGAR ANALYTICS
		Tracker t = getTracker(TrackerName.GLOBAL_TRACKER);
		t.send(new HitBuilders.EventBuilder()
					.setCategory("EventoBotonesAcercaDe")
					.setAction("Click")
					.setLabel("BotonesAcercaDe")
					.build());
	}
	
	
	protected void CargarBannerAd()
	{

		 /*INICIO DEL AD*/
		 // Create an ad.

        AdView adView = new AdView(this);
	    adView.setAdSize(AdSize.SMART_BANNER);
	    adView.setAdUnitId(AD_UNIT_ID);
	    
	    // Add the AdView to the view hierarchy. The view will have no size
	    // until the ad is loaded.
	    RelativeLayout layout = (RelativeLayout)findViewById(R.id.container);
	    RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(
	    		RelativeLayout.LayoutParams.MATCH_PARENT,
	    		RelativeLayout.LayoutParams.WRAP_CONTENT);
	    
	    lay.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	    //LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
	    layout.addView(adView,lay);
	    

	    // Create an ad request. Check logcat output for the hashed device ID to
	    // get test ads on a physical device.
	   
	    
	    
	    AdRequest adRequest = new AdRequest.Builder()
	        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	        .addTestDevice("83806BD0C28F5D013B0B428D8E784C30")//NEXUS 4
	        .build();

	    // Start loading the ad in the background.
	    adView.loadAd(adRequest);
		/*FIN DEL AD*/
	}
	
	private void MostrarAcercaDe(String mensaje, Context con)
    {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(con);
    	dialog.setTitle("Acerca de...");
    	dialog.setMessage(mensaje);
    	dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		});
    	dialog.show();
    }

	

	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_acerca_de,
                    container, false);
		}
	}

}
