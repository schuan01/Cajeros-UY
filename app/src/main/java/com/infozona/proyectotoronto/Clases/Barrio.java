package com.infozona.proyectotoronto.Clases;

public class Barrio 
{
	private String _Departamento;
	private String _Barrio;
	
	public String GetDepartamento()
	{
		return _Departamento;
	}
	
	
	public void SetDepartamento(String d)
	{
		_Departamento = d;
	}
	
	public String GetBarrio()
	{
		return _Barrio;
	}
	
	public void SetBarrio(String b)
	{
		_Barrio = b;
	}
	
	public Barrio()
	{
		SetBarrio("Barrio por defecto");
		SetDepartamento("Departamento por defecto");
	}
	
	public Barrio(String d, String b)
	{
		SetDepartamento(d);
		SetBarrio(b);
	}
	
	@Override
	public String toString()
	{
		return "Departamento: " + GetDepartamento() + " | Barrio: " + GetBarrio();
	}
	
}
