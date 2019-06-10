package com.infozona.proyectotoronto.Clases;

public class Cajero 
{
	
	private int _id;
	private String _nombreLocal;
	private String _direccion;
	private double _latitud;
	private double _longitud;
	private String _tipoCajero; //ATM, Term Autoservicio, POST
	private boolean _aceptaDeposito;
	private String _red;//REDBROU,BANRED
	private Barrio _barrio;
	private String _horario;
	
	
	public int GetId()
	{
		return _id;
	}
	
	public void SetId(int i)
	{
		_id = i;
	}
	
	public String GetNombreLocal()
	{
		return _nombreLocal;
	}
	
	public void SetNombreLocal(String n)
	{
		_nombreLocal = n;
	}
	
	public double GetLatitud()
	{
		return _latitud;
	}
	
	public void SetLatitud(double l)
	{
		_latitud = l;
	}
	
	public double GetLongitud()
	{
		return _longitud;			
	}
	
	public void SetLongitud(double l)
	{
		_longitud = l;
	}
	
	public String GetDireccion()
	{
		return _direccion;
	}
	
	public void SetDireccion(String d)
	{
		_direccion = d;
	}
	
	public String GetTipoCajero()
	{
		return _tipoCajero;
	}
	
	public void SetTipoCajero(String t)
	{
		_tipoCajero = t;
	}
	
	public boolean GetAceptaDeposito()
	{
		return _aceptaDeposito;
	}
	
	public void SetAceptaDeposito(boolean a )
	{
		_aceptaDeposito = a;
	}
	
	public String GetRed()
	{
		return _red;
	}
	
	public void SetRed(String r)
	{
		_red = r;
	}
	
	public Barrio GetBarrio()
	{
		return _barrio;
	}
	
	public void SetBarrio(Barrio b)
	{
		_barrio = b;
	}
	
	
	
	public String GetHorario()
	{
		return _horario;
	}
	
	public void SetHorario(String h)
	{
		_horario = h;
	}
	
	
	public Cajero()
	{
		SetId(0);
		SetLatitud(0);
		SetLongitud(0);
		SetNombreLocal("Cajero por defecto");
		SetTipoCajero("Ninguno");
		SetDireccion("Direccion por defecto");
		SetAceptaDeposito(false);
		SetHorario("Horario por defecto");
		SetBarrio(new Barrio());
		SetRed("Sin Red");
		
		
		
	}
	
	public Cajero(int pId, double pLatitud, double pLongitud, String pNombreLocal,String pDireccion, String pTipoCajero,boolean pAceptaDeposito, String pHorario, String pRed, Barrio pBarrio)
	{
		SetId(pId);
		SetDireccion(pDireccion);
		SetLatitud(pLatitud);
		SetLongitud(pLongitud);
		SetNombreLocal(pNombreLocal);
		SetTipoCajero(pTipoCajero);
		SetAceptaDeposito(pAceptaDeposito);
		SetHorario(pHorario);
		SetBarrio(pBarrio);
		SetRed(pRed);
		
	}

}
