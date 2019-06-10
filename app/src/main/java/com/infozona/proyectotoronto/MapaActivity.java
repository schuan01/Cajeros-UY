package com.infozona.proyectotoronto;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.infozona.proyectotoronto.Clases.Cajero;
import com.infozona.proyectotoronto.Clases.DataBaseHandler;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {
	private GoogleMap map;
	private final LatLng MAPA_INICIAL = new LatLng(-34.883611, -56.181944); //Montevideo


	private final String TAG = "MapaActivity";


	private MarkerOptions marcadorPuntosActual = null;
	private boolean mensajeMostrado = false;
	private DataBaseHandler db = null;


	List<Cajero> lCajeros = null;
	ActionBarDrawerToggle mDrawerToggle;
	ListView mDrawerList;
	DrawerLayout mDrawerLayout;
	GoogleApiClient mGoogleApiClient;
	LocationRequest mLocationRequest = null;


	private boolean iniciarActivity = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);
		buildGoogleApiClient();
		mGoogleApiClient.connect();
		createLocationRequest();

		setContentView(R.layout.activity_mapa);
		getSupportActionBar().setTitle("Cajeros disponibles");
		final ProgressDialog dlgCargando = ProgressDialog.show(this, "Cargando Cajeros...", "Por favor espere...", true);
		EnviarPantalla("MapaActivity");


		//CargarUbicacionActual();
		String[] mPlanetTitles;

		mPlanetTitles = getResources().getStringArray(R.array.opcionesDrawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);


		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}


		} catch (Exception ex) {

		}
		//getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background_enabled));

		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				//R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				//getActionBar().setTitle("SE CERRO");
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				//getActionBar().setTitle("SE ABRIO");
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		new Thread(new Runnable() {

			@Override
			public void run() {
				CrearBD();


				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						CargarMapaGoogleMaps();
						Bundle extras = getIntent().getExtras();

						String eleccion = "";
						if (extras != null) {
							eleccion = extras.getString("Eleccion");

						}

						if (eleccion.equals("BanRed"))
							FiltrarBanredCercanos();
						else if (eleccion.equals("RedBrou"))
							FiltrarRedBrouCercanos();
						else if (eleccion.equals("Todos"))
							LeerTodosCajeros();
						else if (mGoogleApiClient.isConnected()) {
							LeerCajerosCercanos();
						}


						dlgCargando.dismiss();
					}
				});

			}
		}).start();

	}

	@Override
	public void onResume() {

		mGoogleApiClient.connect();
		super.onResume();
		if (iniciarActivity)
			LeerCajerosCercanos();


	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		map = googleMap;

		//mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34.893713,-56.171671)));//Montevideo
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&	checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
				// Should we show an explanation?
				if (ActivityCompat.shouldShowRequestPermissionRationale(MapaActivity.this,
						Manifest.permission.ACCESS_FINE_LOCATION)) {

					// Show an expanation to the user *asynchronously* -- don't block
					// this thread waiting for the user's response! After the user
					// sees the explanation, try again to request the permission.

				} else {

					// No explanation needed, we can request the permission.

					ActivityCompat.requestPermissions(MapaActivity.this,
							new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							1);

					// MY_PERMISSION_REQUEST_READ_FINE_LOCATION is an
					// app-defined int constant. The callback method gets the
					// result of the request.
				}
			} else {
				//Esto pone el marcador del punto azul y el boton de centrar en el mapa
				map.setMyLocationEnabled(true);

			}
		}


		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(mLocationRequest);

		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(LocationSettingsResult result) {
				final Status status = result.getStatus();
				//final LocationSettingsStates loc = result.getLocationSettingsStates();
				switch (status.getStatusCode()) {
					case LocationSettingsStatusCodes.SUCCESS:
						// All location settings are satisfied. The client can
						// initialize location requests here.
						break;
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						// Location settings are not satisfied, but this can be fixed
						// by showing the user a dialog.
						try {
							// Show the dialog by calling startResolutionForResult(),
							// and check the result in onActivityResult().
							status.startResolutionForResult(
									MapaActivity.this,
									0);
						} catch (IntentSender.SendIntentException e) {
							// Ignore the error.
						}
						break;
					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						// Location settings are not satisfied. However, we have no way
						// to fix the settings so we won't show the dialog.
						break;
				}
			}
		});

	}

	/**
	 * Builds a GoogleApiClient.
	 * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
	 */
	private synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(15000);
		mLocationRequest.setFastestInterval(10000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	public enum TrackerName {
		APP_TRACKER,
		GLOBAL_TRACKER,
		ECOMMERCE_TRACKER,
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytic = GoogleAnalytics.getInstance(this);
			//analytic.setDryRun(true);

			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytic.newTracker("UA-60369825-1")
					: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytic.newTracker(R.xml.global_tracker)
					: analytic.newTracker("UA-60369825-1");

			mTrackers.put(trackerId, t);

		}

		return mTrackers.get(trackerId);
	}

	protected void EnviarPantalla(String nombrePantalla) {
		//CARGAR ANALYTICS
		Tracker t = getTracker(TrackerName.GLOBAL_TRACKER);
		t.setScreenName(nombrePantalla);
		t.send(new HitBuilders.AppViewBuilder().build());

	}

	protected void EnviarEventoAnalytics(String categoria, String accion, String label) {
		//CARGAR ANALYTICS
		Tracker t = getTracker(TrackerName.GLOBAL_TRACKER);
		t.send(new HitBuilders.EventBuilder()
				.setCategory(categoria)
				.setAction(accion)
				.setLabel(label)
				.build());
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mDrawerLayout.openDrawer(Gravity.LEFT);
			return true;
		}

		//return false;
		return super.onKeyDown(keyCode, e);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//setContentView(R.layout.activity_mapa);
	}

	protected void CargarUbicacionActual() {
		try {
			
			
			
			
			/*_locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
			
			_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
				
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLocationChanged(Location location) {
					// TODO Auto-generated method stub
					
				}
			});
			
			List<String> listProviders = _locationManager.getProviders(true);
			Location bestLocation = null;
			for(String provider : listProviders)
			{
				
				
				//Location l = _locationManager.getLastKnownLocation(provider);
				if(l == null)
				{
					continue;
				}
				if(bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())
				{
					//_locationManager.requestLocationUpdates(provider,3000,0,MyLocationListener);
					bestLocation = l;
					
				}
			}
			
			marcadorPuntosActual = new MarkerOptions();
			LatLng ubicacion = new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude());
			marcadorPuntosActual.position(ubicacion);*/

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    Activity#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for Activity#requestPermissions for more details.
					return;
				}
			}
			Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

			marcadorPuntosActual = new MarkerOptions();
			LatLng ubicacion = new LatLng(l.getLatitude(), l.getLongitude());
			marcadorPuntosActual.position(ubicacion);
			Log.d("MapaActivity", l.toString());

		} catch (NullPointerException ex) {
			if (!mensajeMostrado) {
				Log.e("MapaActivity", ex.getMessage());
				Dialog dialog = MostrarMensaje(1);
				AlertDialog alerta = (AlertDialog) dialog;
				mensajeMostrado = true;
				alerta.show();
			}
		} catch (Exception ex) {
			Log.e("MapaActiviy", ex.getMessage());
		}
	}

	protected void HacerZoom(String opcion) {
		switch (opcion) {
			case "Montevideo":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.883611, -56.181944), 10.5f));
				break;

			case "Soriano":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.250000, -58.031944), 8.5f));
				break;

			case "Artigas":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-30.488089, -57.101318), 8.5f));
				break;

			case "Salto":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-31.467933, -57.101318), 8.5f));
				break;

			case "Rio Negro":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-32.762540, -57.101318), 8.5f));
				break;

			case "Colonia":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.149864, -57.462725), 8.5f));
				break;

			case "San Jose":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.333333, -56.716666), 8.5f));
				break;

			case "Canelones":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.516666, -56.283333), 8.5f));
				break;

			case "Flores":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.562947, -56.831111), 8.5f));
				break;

			case "Florida":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.815501, -55.846805), 8.5f));
				break;

			case "Durazno":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.078449, -56.024998), 8.5f));
				break;

			case "Maldonado":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.627761, -54.961183), 8.5f));
				break;

			case "Rocha":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.967494, -53.910659), 8.5f));
				break;

			case "Lavalleja":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.004035, -54.961183), 8.5f));
				break;

			case "Treinta y Tres":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.147666, -54.259311), 8.5f));
				break;

			case "Cerro Largo":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-32.202475, -54.259311), 8.5f));
				break;

			case "Rivera":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-31.473415, -55.225920), 8.5f));
				break;

			case "Paysandu":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-32.321388, -58.075555), 8.5f));
				break;

			case "Tacuarembo":

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-31.733333, -55.983333), 8.5f));
				break;
		}
	}

	protected void CargarMapaGoogleMaps() {
		try {
			SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);


			//SET MAP POR DEFECTO
			if (marcadorPuntosActual == null || marcadorPuntosActual.getPosition() == null) {
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAPA_INICIAL, 12.5f));

			} else
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(marcadorPuntosActual.getPosition(), 12.5f));

			//Mi localizacion
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    Activity#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for Activity#requestPermissions for more details.
					return;
				}
			}
			map.setMyLocationEnabled(true);
			
			
			
			map.setOnMapLongClickListener(new OnMapLongClickListener()
			{
				//AGREGAR MARCADOR NUEVO
				Marker marker = null;
				
				@Override
				public void onMapLongClick(LatLng point)
				{
					/*if(marker != null)
					{
						marker.remove();
					}
					marker = map.addMarker(new MarkerOptions().position(point)
							.title("Cajero Nuevo")
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));*/
							
					
				}
			});


		}
		catch(Exception ex)
		{
			Log.e(TAG, "CargarMapaGoogleMaps() " + ex.getMessage());
		}
	}
	
	protected void LeerTodosCajeros()
	{
		try
		{
			//limpiamos todos los puntos
			map.clear();
			
			Log.d("Leyendo: ", "Leyendo todos los cajeros...");
			List<Cajero> lCajeros = db.ListarCajeros();
			LatLng cooredenadas;
			
			
			SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences",MODE_PRIVATE);
			String departam = appPrefs.getString("spnDepartamentosSetting", "Montevideo");//si no existe es Montevideo
			
			
			for(Cajero ca : lCajeros)
			{
				
				
				if(ca.GetBarrio().GetDepartamento().equals(departam) )
				{
					cooredenadas = new LatLng(ca.GetLatitud(),ca.GetLongitud());
					if(ca.GetRed().equals("Banred"))
					{
						map.addMarker(new MarkerOptions().position(cooredenadas)
								 .title(ca.GetId() + " - " + ca.GetNombreLocal())
								 .snippet(ca.GetDireccion())
								 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
						
						String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
						Log.d("Cajero: ",log);
					}
					else
					{
						map.addMarker(new MarkerOptions().position(cooredenadas)
								 .title(ca.GetId() + " - " + ca.GetNombreLocal())
								 .snippet(ca.GetDireccion())
								 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
						
						String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
						Log.d("Cajero: ",log);
					}
					
					
					
				}
				
			}
			
			HacerZoom(departam);
		}
		catch(Exception ex)
		{
			Log.e(TAG, "LeerTodosCajeros() " + ex.getMessage());
		}
		finally
		{
			db.close();
		}
		
	}
	
	
	protected void LeerCajerosCercanos()
	{
		try
		{
			
			//limpiamos todos los puntos
			map.clear();
			
			//obtengo el valor de las opciones
			SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences",MODE_PRIVATE);
			String texto = appPrefs.getString("editTextPref", "3");//si no existe es 3 km
			float pDistanciaMaxima = Float.parseFloat(texto) * 1000; //lo pasamos a metros
			
			Log.d("Leyendo: ", "Leyendo todos los cajeros cercanos...");
			List<Cajero> lCajeros = db.ListarCajeros();
			LatLng cooredenadas;
			float cercanos = 0;
			float distCercanos = 0;
			
			//cargamos la ultima ubicacion
			
			if(mGoogleApiClient.isConnected())
			{
				Log.d("Test", "Conecto en LeerCajerosCercanos()");
				CargarUbicacionActual();
			}
			
			if(marcadorPuntosActual == null || marcadorPuntosActual.getPosition() == null)
			{
				if(mGoogleApiClient.isConnected())
					CargarUbicacionActual();
				
			}

			if(marcadorPuntosActual != null || marcadorPuntosActual.getPosition() != null)
			{
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(marcadorPuntosActual.getPosition(), 12.5f));
				
			}
			
			for(Cajero ca : lCajeros)
			{
				cooredenadas = new LatLng(ca.GetLatitud(),ca.GetLongitud());
				cercanos = MostrarCercanos(marcadorPuntosActual.getPosition(), cooredenadas);
				
				if(ca.GetRed().equals("Banred") && cercanos <= pDistanciaMaxima)
				{
					distCercanos = cercanos / 1000;
					map.addMarker(new MarkerOptions().position(cooredenadas)
							 .title(ca.GetId() + " - " + ca.GetNombreLocal() + "( " + String.format("%.2f",distCercanos) + " Km)")
							 .snippet(ca.GetDireccion())
							 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
				}
				else if(ca.GetRed().equals("RedBrou") && cercanos <= pDistanciaMaxima)
				{
					distCercanos = cercanos / 1000;
					map.addMarker(new MarkerOptions().position(cooredenadas)
							 .title(ca.GetId() + " - " + ca.GetNombreLocal() + "( " + String.format("%.2f",distCercanos) + " Km)")
							 .snippet(ca.GetDireccion())
							 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				}
				
				String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
				Log.d("Cajero: ",log);
				
			}
		}
		catch(Exception ex)
		{
			Log.e(TAG, "LeerCajerosCercanos() " + ex.getMessage());
		}
		finally
		{
			db.close();
		}
		
	}

	protected void CrearBD()
	{
		try 
		{
			//CAJEROS
			db = new DataBaseHandler(this);
			//db.createDataBase();
			db.openDataBase();
		} 
		catch (Exception ex)
		{
			Log.e(TAG, "CrearBD() " + ex.getMessage());
		}
	}

	
	protected void CrearMenu(Menu menu)
	{
		
		MenuItem mnuNormal = menu.add(0,0,0,"Mapa Normal");
		{
			mnuNormal.setAlphabeticShortcut('n');
			//mnuBanred.setIcon(R.drawable.ic_launcher);
		}
		MenuItem mnuSatelital = menu.add(0,1,1,"Mapa Satelital");
		{
			mnuSatelital.setAlphabeticShortcut('s');
			//mnuRedBrou.setIcon(R.drawable.ic_launcher);
		}
		MenuItem mnuHibrido= menu.add(0,2,2,"Hibrido");
		{
			mnuHibrido.setAlphabeticShortcut('h');
			//mnuRedBrou.setIcon(R.drawable.ic_launcher);
		}
		Log.d(TAG, "CrearMenu() " + "Desplegando Menu");
				
		
	}
	protected void FiltrarRedBrou()
	{
		try
		{
			map.clear();
			Log.d("Leyendo: ", "Leyendo Solo RedBrou");
			
			List<Cajero> lCajeros = db.ListarCajeros();
			LatLng cooredenadas;
			//obtengo el valor de las opciones
			SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences",MODE_PRIVATE);
			String departam = appPrefs.getString("spnDepartamentosSetting", "Montevideo");//si no existe es Montevideo
			
			for(Cajero ca : lCajeros)
			{
				
				if(ca.GetBarrio().GetDepartamento().equals(departam) )
				{
					cooredenadas = new LatLng(ca.GetLatitud(),ca.GetLongitud());
					
					if(ca.GetRed().equals("RedBrou"))
					{
						map.addMarker(new MarkerOptions().position(cooredenadas)
								 .title(ca.GetId() + " - " + ca.GetNombreLocal())
								 .snippet(ca.GetDireccion())
								 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
					}
					
	
					String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
					Log.d("Cajero: ",log);
				}
				
			}
		}
		catch(Exception ex)
		{
			Log.e(TAG, "FiltrarRedBrou() " + ex.getMessage());
		}
		finally
		{
			db.close();
		}
	}

	protected void FiltrarBanred()
	{
		try
		{
			//limpiamos todos los puntos
			map.clear();
			
			Log.d("Leyendo: ", "Leyendo Solo Banred");
			List<Cajero> lCajeros = db.ListarCajeros();
			LatLng cooredenadas = null;
			//obtengo el valor de las opciones
			SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences",MODE_PRIVATE);
			String departam = appPrefs.getString("spnDepartamentosSetting", "Montevideo");//si no existe es Montevideo
			
			for(Cajero ca : lCajeros)
			{
				
				if(ca.GetBarrio().GetDepartamento().equals(departam) )
				{
					cooredenadas = new LatLng(ca.GetLatitud(),ca.GetLongitud());
					if(ca.GetRed().equals("Banred"))
					{
						map.addMarker(new MarkerOptions().position(cooredenadas)
								 .title(ca.GetId() + " - " + ca.GetNombreLocal())
								 .snippet(ca.GetDireccion())
								 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
					}
					
	
					String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
					Log.d("Cajero: ",log);
				}
				
			}
		}
		catch(Exception ex)
		{
			Log.e(TAG, "FiltrarBanred() " + ex.getMessage());
		}
		finally
		{
			db.close();
		}
	}
	protected void FiltrarBanredCercanos()
	{
		try
		{
			//limpiamos todos los puntos
			map.clear();
			
			//obtengo el valor de las opciones
			SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences",MODE_PRIVATE);
			String texto = appPrefs.getString("editTextPref", "3");//si no existe es 3 km
			
			float pDistanciaMaxima = Float.parseFloat(texto) * 1000; //lo pasamos a metros
			
			Log.d("Leyendo: ", "Leyendo Solo Banred Cercanos");
			List<Cajero> lCajeros = db.ListarCajeros();
			LatLng cooredenadas = null;
			float cercanos = 0;
			float distCercanos = 0;
			
			//cargamos la ultima ubicacion
			CargarUbicacionActual();
			
			if(marcadorPuntosActual == null || marcadorPuntosActual.getPosition() == null)
			{
				CargarUbicacionActual();
				
			}
			
			if(marcadorPuntosActual != null || marcadorPuntosActual.getPosition() != null)
			{
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(marcadorPuntosActual.getPosition(), 12.5f));
				
			}
			
			for(Cajero ca : lCajeros)
			{
				
					cooredenadas = new LatLng(ca.GetLatitud(),ca.GetLongitud());
					cercanos = MostrarCercanos(marcadorPuntosActual.getPosition(), cooredenadas);
					if(ca.GetRed().equals("Banred") && cercanos <= pDistanciaMaxima)
					{
						distCercanos = cercanos / 1000;
						map.addMarker(new MarkerOptions().position(cooredenadas)
								 .title(ca.GetId() + " - " + ca.GetNombreLocal() + "( " + String.format("%.2f",distCercanos) + " Km)")
								 .snippet(ca.GetDireccion())
								 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
						
						String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
						Log.d("Cajero: ",log);
					}
				
			}
		}
		catch(Exception ex)
		{
			Log.e(TAG, "FiltrarBanredCercanos() " + ex.getMessage());
		}
		finally
		{
			db.close();
		}
	}
	
	protected void FiltrarRedBrouCercanos()
	{
		try
		{
			//limpiamos todos los puntos
			map.clear();
			
			//obtengo el valor de las opciones
			SharedPreferences appPrefs = getSharedPreferences("com.infozona.proyectotoronto_preferences",MODE_PRIVATE);
			String texto = appPrefs.getString("editTextPref", "3");//si no existe es 3 km
			
			float pDistanciaMaxima = Float.parseFloat(texto) * 1000; //lo pasamos a metros
			
			Log.d("Leyendo: ", "Leyendo Solo RedBrou Cercanos");
			List<Cajero> lCajeros = db.ListarCajeros();
			LatLng cooredenadas;
			float cercanos = 0;
			float distCercanos = 0;
			
			//cargamos la ultima ubicacion
			CargarUbicacionActual();
			
			if(marcadorPuntosActual == null || marcadorPuntosActual.getPosition() == null)
			{
				CargarUbicacionActual();
				
			}
			
			
			if(marcadorPuntosActual != null || marcadorPuntosActual.getPosition() != null)
			{
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(marcadorPuntosActual.getPosition(), 12.5f));
				
			}
			
			for(Cajero ca : lCajeros)
			{
					cooredenadas = new LatLng(ca.GetLatitud(),ca.GetLongitud());
					cercanos = MostrarCercanos(marcadorPuntosActual.getPosition(), cooredenadas);
					if(ca.GetRed().equals("RedBrou") && cercanos <= pDistanciaMaxima)
					{
						distCercanos = cercanos / 1000;
						map.addMarker(new MarkerOptions().position(cooredenadas)
								 .title(ca.GetId() + " - " + ca.GetNombreLocal() + "( " + String.format("%.2f",distCercanos) + " Km)")
								 .snippet(ca.GetDireccion())
								 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
						
						String log = "Id: " + ca.GetId() + " Nombre: " + ca.GetNombreLocal() + " Latitud: " + ca.GetLatitud() + " Longitud: " + ca.GetLongitud();
						Log.d("Cajero: ",log);
					}
				
			}
		}
		catch(Exception ex)
		{
			Log.e(TAG, "FiltrarRedBrouCercanos() " + ex.getMessage());
		}
		finally
		{
			db.close();
		}
	}
	
	
	protected boolean SeleccionDeMenu(MenuItem item)
	{
		switch(item.getItemId())
		{
		case 0: //si es NORMAL
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			return true;
		case 1: //si es SATELITAL
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			return true;
		case 2:
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
			
		default:
			
			return false;
		}
	
		
	}
	protected Dialog MostrarMensaje(int id)
	{
		switch(id)
		{
		case 0:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle("Aviso")
			.setPositiveButton("OK", 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();
							
						
						}
					}).create();
		case 1:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle("ErrorCargarMapaGoogleMaps")
			.setMessage("No se pudo obtener la ubicacion")
			.setPositiveButton("Activar", 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							iniciarActivity = true;
							dialog.dismiss();
							
						
						}
					})
			.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			})
			.create();
		}
		return null;
	}
	
	protected float MostrarCercanos(LatLng origen, LatLng destino)
	{
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(destino.latitude - origen.latitude);
		double dLng = Math.toRadians(destino.longitude - origen.longitude);
		double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(destino.latitude))*Math.sin(dLng/2)*Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;
		int meterConversion = 1609;
		
		return new Float(dist * meterConversion).floatValue();
	}
	
	@Override
	public Dialog onCreateDialog(int id)
	{
		return MostrarMensaje(id);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		CrearMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		
		  if (mDrawerToggle.onOptionsItemSelected(item))
	       {
	           return true;
	       }
		  else
			  return SeleccionDeMenu(item);
	}
	
	/* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener 
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position)
    {
    	switch(position)
    	{
    	case 0: //INICIO
    		//Opciones
    		
			/*Intent about = new Intent(Intent.ACTION_CALL);
			about.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			about.setClass(MapaActivity.this, InicioActivity.class);
			EnviarEventoAnalytics("EventoBotonInicio","Click","InicioMapa");
			
			startActivity(about);
			
			MapaActivity.this.finish();
    		break;*/
    	
    	case 1://todos los cajeros del departamento
    		EnviarEventoAnalytics("EventoBotonTodosCajeros","Click","MostrarXDeptoMapa");
			LeerTodosCajeros();
			break;
			
    	case 2: //si es banred por departamento
    		EnviarEventoAnalytics("EventoBotonFiltrarBanred","Click","SoloBanredDeptoMapa");
			FiltrarBanred();
			break;
		case 3: //si es redbrou por departameto
			EnviarEventoAnalytics("EventoBotonFiltrarRedBrou","Click","SoloRedBrouDeptoMapa");
			FiltrarRedBrou();
			break;
		
		case 4://solo cercanos todo
			EnviarEventoAnalytics("EventoLeerCajerosCercanos","Click","LeerCercanosMapa");
			LeerCajerosCercanos();
			break;
			
		case 5://solo cercanos RedBrou
			EnviarEventoAnalytics("EventoFiltrarRedBrouCercanos","Click","FiltrarRedBrouCercanosMapa");
			FiltrarRedBrouCercanos();
			break;
			
		case 6://solo cercanos Banred
			EnviarEventoAnalytics("EventoFiltrarBanredCercanos","Click","FiltrarBanreduCercanosMapa");
			FiltrarBanredCercanos();
			break;
		case 7:
			//Opciones
			Intent about1 = new Intent(MapaActivity.this,SettingsActivity.class);
			EnviarEventoAnalytics("EventoOpciones","Click","OpcionesMapa");
			iniciarActivity= true;
			startActivity(about1);
			break;
		case 8:
			Intent about2 = new Intent(MapaActivity.this,AcercaDe.class);
			EnviarEventoAnalytics("EventoAcercaDe","Click","AcercaDeMapa");
			iniciarActivity=false;
			startActivity(about2);
			break;
			
		default:
			
			break;
    		
    		
    		
    		
    	
    	}
    	mDrawerList = (ListView)findViewById(R.id.left_drawer);
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
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
			return inflater.inflate(R.layout.fragment_mapa, container,
					false);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint)
	{
		if(mGoogleApiClient.isConnected())
		{
		Log.i("MapaActivity","Conecto!");
		CargarUbicacionActual();
		}
		/*Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		marcadorPuntosActual = new MarkerOptions();
		LatLng ubicacion = new LatLng(l.getLatitude(),l.getAltitude());
		marcadorPuntosActual.position(ubicacion);*/
		
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.e("MapaActivity", "Conexion suspendida: " + cause );
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		Log.e("MapaActivity", "Fallo al conectar");
		// TODO Auto-generated method stub
		
	}

}
