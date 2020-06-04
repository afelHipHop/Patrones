package edu.utilidades;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Cargador {
    //atributos del manifiesto      
    private final static String CLAVE_DE_COMPONENTE="COMPONENTE";
    private final static String NOMBRE_DEL_COMPONENTE="nombre";
    private final static String CLASE_BASE_DEL_COMPONENTE="clase";
    private String directorioDeComponentes;  
    public class InfoComponente{
    	String nombre;
    	String clase;
    	public InfoComponente(String nc, String clase) {
    		this.nombre=nc;this.clase=clase;;		
    	}
    	public String getNombre(){
    		return nombre;
    	}
        public String getClase(){
        	return clase;
        }
    }
    class CargadorRegistro{
    	public String categoria;
    	public URL urls[];
    	public URLClassLoader cargador;
        public HashMap<String,InfoComponente> registroDeComponentes = new HashMap<String,InfoComponente>(); 
    }
    List <CargadorRegistro>listaDeCargadoresRegistro;
    public Cargador(String rutaDeUbicacion){
		listaDeCargadoresRegistro = new ArrayList<CargadorRegistro>();
		cargarComponentesDesdeLaRutaDeUbicacion(rutaDeUbicacion,"");
	}
	boolean yaHaSidoCargadaLaCategoria(String categoria){
		for(int i=0;i<listaDeCargadoresRegistro.size();i++){
			if(listaDeCargadoresRegistro.get(i).categoria.equals(categoria)){
				return true;
			}
		}
		return false;
	}
    private void  cargarComponentesDesdeLaRutaDeUbicacion(String rutaDeUbicacion,String categoria) { 
    	//////////////////////////////
    	if(yaHaSidoCargadaLaCategoria(categoria))return;//esto evita tener diversas cargadores para una misma categoria
    	//////////////////////////////
        if(rutaDeUbicacion == null) {
            directorioDeComponentes =".";
        }else{
        	directorioDeComponentes=rutaDeUbicacion;
        }
        CargadorRegistro cr=new CargadorRegistro();
        cr.categoria = categoria;
        URL[] pluginsClassPath = cargarComponentes(cr.registroDeComponentes);        
        // Creacion de un classloader propio, que registre los plugins en su classpath
        if(pluginsClassPath!=null){
        	cr.urls = pluginsClassPath;
        	cr.cargador = new URLClassLoader(pluginsClassPath, ClassLoader.getSystemClassLoader());
        	listaDeCargadoresRegistro.add(cr);
        }
    }    
   private URL[] cargarComponentes(HashMap<String,InfoComponente> registroDeComponentes) {              
        File f = new File(directorioDeComponentes);                               
        if (!f.canRead() || !f.isDirectory()) {            
            System.out.println("Error: No existe el directorio");            
            return null;
        }                        
       File[] files = f.listFiles(         
            new FileFilter() {                
                public boolean accept(File fileToBeFiltered) {                    
                    return fileToBeFiltered.getName().endsWith(".jar");
                }
            }
        );            
        String nc=null;
        String ncbc=null;       
        ArrayList<URL> urls = new ArrayList<URL>();       
        for (int i = 0; i < files.length ; i++) {
            try {                
                //leer la confighuracion del manifiesto que definen un plugin.                
                JarFile archivo = new JarFile(files[i]); 
                Manifest m= archivo.getManifest();
                Attributes atributos = m.getAttributes(CLAVE_DE_COMPONENTE); 
                //los trim aplicados de una vez sin preguntar son los caracteristicas que como minimo tiene un compoennet
                nc   = atributos.getValue(NOMBRE_DEL_COMPONENTE).trim();
                ncbc = atributos.getValue(CLASE_BASE_DEL_COMPONENTE).trim();
                ////////////////////////////////////////////////////////////
                InfoComponente ip=new InfoComponente(nc,ncbc); 
                urls.add(files[i].toURI().toURL());                                                                
                registroDeComponentes.put(nc,ip);  
                archivo.close();
            } catch(Exception e) {}
        }       
        return (URL[]) urls.toArray(new URL[0]);        
    }
   @SuppressWarnings("rawtypes")
   private  Class getClase(String nombre,int n){
	   Class clase = null;
		try {
			if(n<listaDeCargadoresRegistro.size()){
				String cls = listaDeCargadoresRegistro.get(n).registroDeComponentes.get(nombre).clase;
			    clase = listaDeCargadoresRegistro.get(n).cargador.loadClass(cls);
			 }
		} catch (Exception e) {
			clase = getClase(nombre,n+1);
		}
		return clase; 
   }
   @SuppressWarnings("rawtypes")
   public Class getClase(String nombreDeLaClase){
	     return getClase(nombreDeLaClase,0);     
   }   
}
