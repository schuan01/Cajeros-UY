package com.infozona.proyectotoronto;

import java.io.File;
import java.io.FileWriter;
import android.app.Activity;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.infozona.proyectotoronto.Clases.*;

public class GuardarActivity extends Activity {

	Spinner spinner = null;
	Spinner spinnerDeptos = null;
	ArrayAdapter<CharSequence> adaptador = null; 
	ArrayAdapter<CharSequence> adaptadorDeptos = null; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guardar);
		
		Bundle extras = getIntent().getExtras();
		String latitud = "";
		String longitud = "";
		
		if(extras != null)
		{
			 latitud = extras.getString("Latitud");
			 longitud = extras.getString("Longitud");
			 
		}
		
		TextView txtLatitud = (TextView)findViewById(R.id.txtLatitud);
		txtLatitud.setText(latitud);
		
		TextView txtLongitud = (TextView)findViewById(R.id.txtLongitud);
		txtLongitud.setText(longitud);
		
		
		
		spinner = (Spinner)findViewById(R.id.spnTipos);
		adaptador = ArrayAdapter.createFromResource(this, R.array.tipoCajero,android.R.layout.simple_spinner_item);
		adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adaptador);
		
		spinnerDeptos = (Spinner)findViewById(R.id.spnDepartamentos);
		adaptadorDeptos = ArrayAdapter.createFromResource(this, R.array.departamento,android.R.layout.simple_spinner_item);
		adaptadorDeptos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerDeptos.setAdapter(adaptadorDeptos);
		
		
		//Guardar Cajero
		Button btnGuardar = (Button)findViewById(R.id.btnGuardarCajero);
		btnGuardar.setOnClickListener(new OnClickListener() 
		{
			
			
			
			@Override
			public void onClick(View arg0) 
			{
				GuardarCajero();
				
			}
		});
		
		
		
		
		
		
	}
	
	private void GuardarCajero()
	{
		
		
		Cajero nuevoCajero = new Cajero();
		
		Bundle extras = getIntent().getExtras();
		String latitud = "";
		String longitud = "";
		if(extras != null)
		{
			 latitud = extras.getString("Latitud");
			 longitud = extras.getString("Longitud");
		}
		
		TextView txtLatitud = (TextView)findViewById(R.id.txtLatitud);
		txtLatitud.setText(latitud);
		
		TextView txtLongitud = (TextView)findViewById(R.id.txtLongitud);
		txtLongitud.setText(longitud);
		
		TextView txtNombre = (TextView)findViewById(R.id.txtNombre);
		TextView txtDireccion = (TextView)findViewById(R.id.txtDireccion);
		
		
		
		
		String direccion = txtDireccion.getText().toString();
		String nombre = txtNombre.getText().toString();
		
		nuevoCajero.SetId(1);
		nuevoCajero.SetDireccion(direccion);
		nuevoCajero.SetLatitud(Double.parseDouble(latitud));
		nuevoCajero.SetLongitud(Double.parseDouble(longitud));
		nuevoCajero.SetNombreLocal(nombre);
		nuevoCajero.SetRed(spinner.getSelectedItem().toString());
		nuevoCajero.SetBarrio(new Barrio(spinnerDeptos.getSelectedItem().toString(),spinnerDeptos.getSelectedItem().toString()));
		
		//CAJEROS
		DataBaseHandler db = new DataBaseHandler(this);
		db.createDataBase();
		db.openDataBase();
		//db.EliminarCajeros();
				
		Log.d("Insercion: ", "Insertando...");
		db.AgregarCajero(nuevoCajero);
		
		
		//Escribimos en CSV
		EscribirCSV(nuevoCajero);
		
		//Cerramos la actividad
		GuardarActivity.this.finish();
				
		
	}
	
	private void EscribirCSV(Cajero ca)
	{
		//ESCRIBIR CSV
				File folder;
				folder = new File(Environment.getExternalStorageDirectory() + "/CajerosUY");
					
				boolean existe = false;
				if(!folder.exists())
					existe = folder.mkdir();
				
				Log.d("CarpetaNombre",""+ existe);
				
				final String filename = folder.toString() + "/" + "Cajeros.csv";
				FileWriter fw = null;
				try
				{
					fw = new FileWriter(filename,true);
			
					fw.append(String.valueOf(ca.GetId()));
					fw.append(',');
									
					fw.append(ca.GetNombreLocal());
					fw.append(',');
									
					fw.append(ca.GetDireccion());
					fw.append(',');
					
					fw.append(String.valueOf(ca.GetLatitud()));
					fw.append(',');
									
					fw.append(String.valueOf(ca.GetLongitud()));
					fw.append(',');
					
					fw.append(ca.GetTipoCajero());
					fw.append(',');
					
					fw.append(String.valueOf(ca.GetAceptaDeposito()));
					fw.append(',');
					
					fw.append(ca.GetRed());
					fw.append(',');
					
					fw.append(ca.GetBarrio().GetDepartamento());
					fw.append(',');
					
					fw.append(ca.GetBarrio().GetBarrio());
					fw.append(',');
					
					fw.append(ca.GetHorario());
													
					fw.append('\n');
					
					
				
				}
				catch(Exception ex)
				{
					Log.d("Exception Archivo",ex.getMessage());
				}
				finally
				{
					try
					{
                        if (fw != null) {
                            fw.close();
                        }
                    }
					catch(Exception ex)
					{
						Log.d("Exception Archivo",ex.getMessage());
					}
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
            return inflater.inflate(R.layout.fragment_guardar,
                    container, false);
		}
	}

}
