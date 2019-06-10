package com.infozona.proyectotoronto;





import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.legacy.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.infozona.proyectotoronto.Clases.DataBaseHandler;


public class InicioActivity extends AppCompatActivity
{

	ActionBarDrawerToggle mDrawerToggle;
	 /** The view to show the ad. */
	  private AdView adView;
	  InterstitialAd popUpBannerAd;
	  /* Your ad unit id. Replace with your actual ad unit id. */
	  //private static final String AD_UNIT_ID = "ca-app-pub-8382275640200211/5354482284";
	  //private static final String AD_UNIT_ID_POPUP = "ca-app-pub-8382275640200211/7602609088";
	  private static final String AD_UNIT_ID = "ca-app-pub-2671808875739238/7437109505";
	  private static final String AD_UNIT_ID_POPUP = "ca-app-pub-2671808875739238/8913842708";
	  Spinner spinnerDeptos = null;
	  ArrayAdapter<CharSequence> adaptadorDeptos = null;


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
					.setCategory("EventoBotones")
					.setAction("Click")
					.setLabel("Botones")
					.build());
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inicio);

        try {

            //ANALYTICS
            EnviarPantalla("InicioActivity");
			MobileAds.initialize(this,"ca-app-pub-2671808875739238~5960376305");

            //CARGAR PROPAGANDA
            CargarBannerAd();
            CargarPopUpBannerAd();//no se muestra aun
            CrearBD();

            //Cargar Settings y Spinner
            spinnerDeptos = (Spinner) findViewById(R.id.spnDepartamentosInicio);
            adaptadorDeptos = ArrayAdapter.createFromResource(this, R.array.departamento, android.R.layout.simple_spinner_item);
            adaptadorDeptos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDeptos.setAdapter(adaptadorDeptos);

            SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences", MODE_PRIVATE);
            String departam = appPrefs.getString("spnDepartamentosSetting", "Montevideo");//si no existe es Montevideo
            int pos = adaptadorDeptos.getPosition(departam);
            spinnerDeptos.setSelection(pos);


            //BOTON INCIO
            Button btnInicio = (Button) findViewById(R.id.btnInicio);
            btnInicio.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    EnviarEventoAnalytics();
                    Intent about = new Intent(InicioActivity.this, MapaActivity.class);
                    startActivity(about);

                }
            });

            //BOTON Mostrar Todos Banred
            Button btnMostrarBanred = (Button) findViewById(R.id.btnMostrarSoloBanred);
            btnMostrarBanred.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    EnviarEventoAnalytics();
                    Intent about = new Intent(InicioActivity.this, MapaActivity.class);
                    about.putExtra("Eleccion", "BanRed");
                    startActivity(about);

                }
            });

            //BOTON Mostrar Todos RedBrou
            Button btnMostrarRedBrou = (Button) findViewById(R.id.btnMostrarSoloRedBrou);
            btnMostrarRedBrou.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    EnviarEventoAnalytics();
                    Intent about = new Intent(InicioActivity.this, MapaActivity.class);
                    about.putExtra("Eleccion", "RedBrou");
                    startActivity(about);

                }
            });

            //BOTON Mostrar Todos RedBrou
            Button btnMostrarPorDepartamento = (Button) findViewById(R.id.btnMostrarPorDepartamento);
            btnMostrarPorDepartamento.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = appPrefs.edit();
                    prefsEditor.putString("spnDepartamentosSetting", String.valueOf(spinnerDeptos.getSelectedItem().toString()));
                    prefsEditor.apply();

                    EnviarEventoAnalytics();
                    Intent about = new Intent(InicioActivity.this, MapaActivity.class);
                    about.putExtra("Eleccion", "Todos");
                    startActivity(about);

                }
            });

            getSupportActionBar().setTitle("Inicio");
        }
        catch(Exception ex)
        {
            Log.e("InicioActivity", "onCreate() " + ex.getMessage());
        }
		
		
		
	}
	
	protected void CrearBD()
	{
		try 
		{
			//CAJEROS
            DataBaseHandler db = new DataBaseHandler(this);
			db.createDataBase();
			db.openDataBase();
		} 
		catch (Exception ex)
		{
			Log.e("InicioActivity", "CrearBD() " + ex.getMessage());
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
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
	    
	    lay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    //lay.addRule(RelativeLayout.BELOW);
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
	protected void CargarPopUpBannerAd()
	{
		popUpBannerAd = new InterstitialAd(InicioActivity.this);
		popUpBannerAd.setAdUnitId(AD_UNIT_ID_POPUP);
		AdRequest adRequest = new AdRequest.Builder()
		        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		        .addTestDevice("83806BD0C28F5D013B0B428D8E784C30")//NEXUS 4
		        .build();
		popUpBannerAd.loadAd(adRequest);
		//cargo pero no muestro
		
		popUpBannerAd.setAdListener(new AdListener() {
			
			public void onAdLoaded()
			{
				
			}
		});
	}
	
	@Override
	public void onBackPressed()
	{
		if(popUpBannerAd != null && popUpBannerAd.isLoaded())
		{
			popUpBannerAd.show();
		}
		
		
		super.onBackPressed();
	}
	@Override
	public void onResume() {
	    super.onResume();
	    if (adView != null) {
	      adView.resume();
	    }
	  }

	  @Override
	public void onPause() {
	    if (adView != null) {
	      adView.pause();
	    }
	    super.onPause();
	  }

	/** Called before the activity is destroyed. */
	  @Override
	public void onDestroy() {
	    // Destroy the AdView.
	    if (adView != null) {
	      adView.destroy();
	    }
	    
	    
	    super.onDestroy();
	  }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{

		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)

	{
           return super.onOptionsItemSelected(item);
    }
}





