package com.infozona.proyectotoronto;

import java.util.HashMap;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.preference.PreferenceFragment;


public class SettingsActivity extends AppCompatActivity
{

	Spinner spinnerDeptos = null;
	ArrayAdapter<CharSequence> adaptadorDeptos = null;
	
	private AdView adView;
	private static final String AD_UNIT_ID = "ca-app-pub-2671808875739238/6451400709";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

        try {
            getSupportActionBar().setTitle("Opciones");

            //CARGAR PROPAGANDA
            CargarBannerAd();

            //ANALYTIC
            EnviarPantalla("SettingsActivity");


            try {
                spinnerDeptos = (Spinner) findViewById(R.id.spnDepartamentoSettings);
                adaptadorDeptos = ArrayAdapter.createFromResource(this, R.array.departamento, android.R.layout.simple_spinner_item);
                adaptadorDeptos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDeptos.setAdapter(adaptadorDeptos);


                SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences", MODE_PRIVATE);
                String departam = appPrefs.getString("spnDepartamentosSetting", "Montevideo");//si no existe es Montevideo

                int pos = adaptadorDeptos.getPosition(departam);
                spinnerDeptos.setSelection(pos);

                String texto = appPrefs.getString("editTextPref", "3");//muestra 3 si no existe
                NumberPicker picker = (NumberPicker) findViewById(R.id.npiDistancia);

                picker.setMinValue(1);
                picker.setMaxValue(1000);
                picker.setWrapSelectorWheel(false);
                picker.setValue(Integer.parseInt(texto));

            } catch (Exception ex) {
                Log.e("Error", ex.getMessage());
            }


            Button btnGuardar = (Button) findViewById(R.id.btnGuardar);
            btnGuardar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    EnviarEventoAnalytics();
                    SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = appPrefs.edit();
                    prefsEditor.putString("editTextPref", String.valueOf(((NumberPicker) findViewById(R.id.npiDistancia)).getValue()));
                    prefsEditor.putString("spnDepartamentosSetting", String.valueOf(spinnerDeptos.getSelectedItem().toString()));
                    prefsEditor.apply();

                    SettingsActivity.this.finish();


                }
            });
        }
        catch(Exception ex)
        {
            Log.e("SettingsActivity", "onCreate() " + ex.getMessage());
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
					.setCategory("EventoBotonesSettings")
					.setAction("Click")
					.setLabel("BotonesSettings")
					.build());
	}
	
	protected void CargarBannerAd()
	{

		 /*INICIO DEL AD*/
		 // Create an ad.
		
	    adView = new AdView(this);
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
	
	

	public static class PrefsFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
			
		}
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
            return inflater.inflate(R.layout.fragment_settings,
                    container, false);
		}
	}

}
