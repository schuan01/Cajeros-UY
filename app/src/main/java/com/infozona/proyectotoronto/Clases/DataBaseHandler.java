package com.infozona.proyectotoronto.Clases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;


public class DataBaseHandler extends SQLiteOpenHelper
{
		//VERSION
		private static final int DATABASE_VERSION = 1;
		
		//BASE DE DATOS
		private SQLiteDatabase mDataBase;
		
		//CONTEX
		private final Context mContext;
		
		//FLAG SI YA LO CREO
		private boolean creado = false;
		
		//UBICACION
		private static String DB_PATH = "";
		
		//NOMBRE BD
		private static final String DATABASE_NAME = "Cajeros.sqlite";
		
		//TABLA CAJERO
		private static final String TABLE_CAJERO = "cajero";
		
		//COLUMNAS
		private static final String CAJEROID = "Id";
		private static final String CAJERONOMBRELOCAL = "NombreLocal";
		private static final String CAJERODIRECCION = "Direccion";
		private static final String CAJEROLATITUD = "Latitud";
		private static final String CAJEROLONGITUD = "Longitud";
		private static final String CAJEROTIPOCAJERO = "TipoCajero";
		private static final String CAJEROACEPTADEPOSITO= "AceptaDeposito";
		private static final String CAJERORED= "Red";
		private static final String CAJERODEPARTAMENTO = "Departamento";
		private static final String CAJEROBARRIO = "Barrio";
		private static final String CAJEROHORARIO = "Horario";
		
		//TAG PARA LOG
		private static final String TAG = "DataBaseHandler";
		
		
		
		public DataBaseHandler(Context context)
		{
			super(context,DATABASE_NAME,null,DATABASE_VERSION);
			
			//AGREGADO GOOGLE
			if(android.os.Build.VERSION.SDK_INT >= 17)
			{
				DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
			}
			else
			{
				DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
			}
			
			this.mContext = context;
			
		}
		
		//CREANDO TABLAS
		//LO DEJAMOS EN DESUSO DEBIDO A QUE SACAMOS LA INFO DE UNA BASE CREADA
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			
		
			/*String CREATE_TABLE_CAJERO = "CREATE TABLE " + TABLE_CAJERO + "("
										 + CAJEROID + " INTEGER PRIMARY KEY, " +
										 CAJERONOMBRE + " TEXT," +
										 CAJERODIRECCION + " TEXT," +
										 CAJEROLATITUD + " REAL, " +
										 CAJEROLONGITUD+ " REAL," +
										 CAJEROTIPO + " TEXT" + ")";
			db.execSQL(CREATE_TABLE_CAJERO);*/
		}
		
		//ACTUALIZANDO LA TABLA
		@Override
		public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion)
		{
			//ELMINAMOS LA ANTERIOR
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAJERO);
			
			//Creamos la tabla de nuevo
			onCreate(db);
		}
		
		//Chequeamos la BD
		private boolean checkDataBase()
		{
			File dbFile = new File(DB_PATH + DATABASE_NAME);
			return dbFile.exists();
			
		}
		
		//COPIAR LA BASE DE DATOS DE ASSESTS A LA UBICACION DEL SISTEMA
		private void copyDataBase() throws IOException
		{
			InputStream _Input = mContext.getAssets().open(DATABASE_NAME);
			String outFileName = DB_PATH + DATABASE_NAME;
			OutputStream mOutput = new FileOutputStream(outFileName);
			byte[] mBuffer = new byte[1024];
			int mLenght;
			while((mLenght = _Input.read(mBuffer))>0)
			{
				mOutput.write(mBuffer,0,mLenght);
			}
			mOutput.flush();
			mOutput.close();
			_Input.close();
		}
		
		//ABRIMOS LA BD
		public boolean openDataBase()
		{
			String mPath = DB_PATH + DATABASE_NAME;
			mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
			return mDataBase != null;
			
		}
		
		//CREAR BASE DE DATOS SI NO EXISTE
		/**
		 * 
		 */
		public void createDataBase()
		{
			//AGREGADO
			if(!creado)
			{
					boolean _DataBaseExist = checkDataBase();
					if(!_DataBaseExist)
					{
						this.getReadableDatabase();
						this.close();try
						{
							copyDataBase();
							creado = true;
							Log.e("BASE DE DATOS", "createDataBase() Base de Datos Creada con Exito");
						}
						catch(IOException e)
						{
							throw new Error("Error al Copiar BD");
						}
					}
					else if(_DataBaseExist)
					{
						File dbFile = new File(DB_PATH + DATABASE_NAME);
						dbFile.delete();
						
						this.getReadableDatabase();
						this.close();try
						{
							copyDataBase();
							creado = true;
							Log.e("BASE DE DATOS", "createDataBase() Base de Datos Creada con Exito");
						}
						catch(IOException e)
						{
							throw new Error("Error al Copiar BD");
						}
					}
			}
					
					
		}
		
		
		//Creamos un Cajero
		public void AgregarCajero(Cajero c)
		{
			
			try 
			{
				SQLiteDatabase db = this.getWritableDatabase();
				ContentValues values = new ContentValues();
				//values.put(CAJEROID, c.GetId());
				values.put(CAJERONOMBRELOCAL, c.GetNombreLocal());
				values.put(CAJERODIRECCION, c.GetDireccion());
				values.put(CAJEROLATITUD, c.GetLatitud());
				values.put(CAJEROLONGITUD, c.GetLongitud());
				values.put(CAJEROTIPOCAJERO, c.GetTipoCajero());
				values.put(CAJEROACEPTADEPOSITO, c.GetAceptaDeposito());
				values.put(CAJERORED, c.GetRed());
				values.put(CAJERODEPARTAMENTO, c.GetBarrio().GetDepartamento());
				values.put(CAJEROBARRIO, c.GetBarrio().GetBarrio());
				values.put(CAJEROHORARIO, c.GetHorario());
				db.insert(TABLE_CAJERO, null, values);
				db.close();
			} catch (Exception e) 
			{
				Log.e(TAG, "AgregarCajero() " + e.getMessage());
				throw e;
				
			}
		}
		
		//Buscamos El Cajero
		/*public Cajero BuscarCajero(int id)
		{
			SQLiteDatabase db = this.getReadableDatabase();
			
			Cursor cursor = db.query(TABLE_CAJERO, new String[] {CAJEROID,CAJERONOMBRELOCAL,CAJERODIRECCION,CAJEROLATITUD,CAJEROLONGITUD,CAJEROTIPOCAJERO}, CAJEROID + "=?",
									 new String[] {String.valueOf(id)}, null, null, null, null);
			if(cursor != null)
				cursor.moveToFirst();
			
			int cId = Integer.parseInt(cursor.getString(0));
			double lat = Double.parseDouble(cursor.getString(3));
			double lon = Double.parseDouble(cursor.getString(4));
			String nombre = cursor.getString(1);
			String dir = cursor.getString(2);
			String tipo = cursor.getString(5);
			
			Cajero c = new Cajero(cId,lat,lon,nombre,dir,tipo);
			
			return c;
		}*/
		
		//Listamos todos los cajeros
		public List<Cajero> ListarCajeros()
		{
			List<Cajero> lCajeros;
			
			try 
			{
				lCajeros = new ArrayList<Cajero>();
				String seleccion = "SELECT * FROM " + TABLE_CAJERO;
				SQLiteDatabase db = this.getReadableDatabase();
				Cursor cursor = db.rawQuery(seleccion, null);
				int cId = 0;
				double lat = 0;
				double lon = 0;
				String nombre = "";
				String dir = "";
				String tipo = "";
				boolean deposito = false;
				String red = "";
				Barrio barrio = null;
				
				String horario = "";
				if (cursor.moveToFirst()) 
				{
					do 
					{
						cId = Integer.parseInt(cursor.getString(0));
						lat = Double.parseDouble(cursor.getString(3));
						lon = Double.parseDouble(cursor.getString(4));
						nombre = cursor.getString(1);
						dir = cursor.getString(2);
						tipo = cursor.getString(5);
						deposito = Boolean.parseBoolean(cursor.getString(6));
						red = cursor.getString(7);
						barrio = new Barrio(cursor.getString(8), cursor.getString(9));
						
						Cajero c = new Cajero(cId, lat, lon, nombre, dir, tipo,deposito,horario,red,barrio);

						lCajeros.add(c);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) 
			{
				Log.e(TAG, "ListarCajeros() " + e.getMessage());
				throw e;
			}
			return lCajeros;
			
			
		}
		
		public void EliminarCajeros()
		{
			try {
				//ELMINAMOS
				SQLiteDatabase db = this.getWritableDatabase();
				db.execSQL("DELETE FROM " + TABLE_CAJERO);
			} catch (Exception e)
			{
				Log.e(TAG, "EliminarCajeros() " + e.getMessage());
				throw e;
			}
			
		}
		
		@Override
		public synchronized void close()
		{
			if(mDataBase != null)
				mDataBase.close();
			super.close();
		}
}
