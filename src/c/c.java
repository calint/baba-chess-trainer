package c;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
final public class c{
	public static String host="localhost";
	public static int port=80;
	public static int nclients=1;
	public static String run="get";
	public static String uri="/";
	public static boolean preq=true;
	public static boolean presph=true;
	public static boolean pcontent=true;
	public static boolean pstats=true;
	public static String clientt="c.s.script";
	public static void main(final String[]args)throws Throwable{
		if(!class_init(c.class,args))return;
		for(int i=0;i<nclients;i++)Class.forName(clientt).newInstance();
//		client.loop();
	}
	public static void class_printopts(final Class<?>cls)throws Throwable{
		for(final Field f:cls.getFields()){
			final Object o=f.get(null);
			out.print(f.getName());out.print("=");
			final Class<?>c=f.getType();
			final boolean stdtype=String.class==c||int.class==c||boolean.class==c;
			if(!stdtype){out.print(f.getType().getName());out.print("(");}
			out.print(o==null?"":o.toString().replaceAll("\\n","\\\\n"));
			if(!stdtype)out.print(")");
			out.println();
		}
	}
	public static boolean class_init(final Class<?>cls,final String[]args)throws Throwable{
		if(args==null||args.length==0)return true;
		if("-1".equals(args[0])){class_printopts(cls);return false;}
		for(int i=0;i<args.length;i+=2){
			final String fldnm=args[i];
			final Field fld=cls.getField(fldnm);
			final String val=args[i+1];
			final Class<?>fldcls=fld.getType();
			if(fldcls.isAssignableFrom(String.class))fld.set(null,val);
			else if(fldcls.isAssignableFrom(int.class))fld.set(null,Integer.parseInt(val));
			else if(fldcls.isAssignableFrom(boolean.class))fld.set(null,"1".equals(val)||"true".equals(val)||"yes".equals(val)||"y".equals(val)?Boolean.TRUE:Boolean.FALSE);
			else if(fldcls.isAssignableFrom(long.class))fld.set(null,Long.parseLong(val));
		}
		return true;
	}
	static PrintStream out=System.out;
	static PrintStream err=System.err;
	public static String tostr(final ByteBuffer bb){if(bb==null)return"";try{return new String(bb.array(),bb.position(),bb.remaining(),"utf8");}catch(UnsupportedEncodingException e){throw new Error(e);}}
	public static String tostr(final ByteBuffer bb,final int len){if(bb==null)return"";try{return new String(bb.array(),bb.position(),len,"utf8");}catch(UnsupportedEncodingException e){throw new Error(e);}}
}