package b;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TimeZone;
final public class b{
	public final static String strenc="utf-8";
	public final static String q=" ڀ ";
	public final static String a=" ํ ";
	public final static int K=1024;
	public final static int M=K*K;
	public final static long G=K*M;
	public final static long T=K*G;
	public final static long P=K*T;
	public final static String pathsep="/";
	public static @conf String hello="public domain server #1";
	public static String id=""+(int)Math.floor(Math.random()*10000);//? fixedwidth
	public static @conf String root_dir=".";
	public static @conf(reboot=true)String server_port=ensure(System.getProperty("app.port"),"8888");
	public static @conf boolean print_requests=false;
	public static @conf boolean print_reply_headers=false;
	public static @conf boolean print_replies=false;
	public static @conf boolean try_file=true;
	public static @conf boolean try_rc=true;
	public static @conf(reboot=true,note="requires reboot to turn on")boolean thd_watch=true;
	public static @conf @unit(name="ms")int thd_watch_sleep_in_ms=10000;
	public static @conf @unit(name="ms")int thd_watch_report_every_ms=60000;
	public static @conf(reboot=true)boolean thread_pool=true;
	public static @conf int thread_pool_size=16;
	public static @conf @unit(name="ms")long thread_pool_lftm=60*1000;
	public static @conf boolean cache_uris=true;
	public static @conf boolean cache_files=true;
	public static @conf(reboot=true)int cache_files_hashlen=K;
	public static @conf @unit(name="B")int cache_files_maxsize=64*K;
	public static @conf @unit(name="ms")long cache_files_validate_dt=1000;
	public static @conf boolean allow_partial_content_from_cache=true;
	public static @conf @unit(name="B")int transfer_file_write_size=256*K;
	public static @conf @unit(name="B")int io_buf_B=64*K;
	public static @conf @unit(name="B")int chunk_B=4*K;
	public static @conf @unit(name="B")int reqinbuf_B=4*K;
	public static @conf String default_directory_file="index.html";
	public static @conf String default_package_class="$";
	public static @conf boolean gc_before_stats=false;
	public static @conf int hash_size_session_values=32;
	public static @conf int hash_size_sessions_store=4*K;
	public static @conf String sessionfile="session.ser";
	public static @conf boolean sessionfile_load=true;
	public static @conf String sessions_dir="u";
	public static @conf boolean cacheu_tofile=true;
	public static @conf String cacheu_dir="/cache/";
	public static @conf final String webobjpkg="a.";
	public static @conf String datetimefmtstr="yyyy-MM-dd HH:mm:ss.sss";
	public static @conf @unit(name="tms")long resources_lastmod=0;
	public static @conf boolean resources_enable_any_path=false;
	public static Set<String>resources_paths=new HashSet<String>(Arrays.asList("x.js","x.css"));
	public static @conf boolean enable_upload=true;
//	public static boolean enable_ssl=false;
//	public static boolean enable_cluster=false;
	public static @conf int max_pending_connections=20000;// when overrun causes SYN flood warning
	public static @conf boolean tcpnodelay=true;
	public static @conf boolean sessions_save_at_shutdown=true;
	public static boolean cloud_bees=false;
	public static @conf boolean print_conf_at_startup=true;
	public static @conf boolean print_stats_at_startup=true;
	public static @conf boolean acl_on=true;
	public static @conf boolean firewall_on=true;
	public static @conf boolean firewall_paths_on=true;
	public static @conf boolean log_client_disconnects=false;
	
	public static @conf @unit(name="tms")long timeatload=System.currentTimeMillis();
	public static @conf String timeatloadstrhtp=tolastmodstr(timeatload);
	public static PrintStream out=System.out;
	public static PrintStream err=System.err;
	private final static LinkedList<req>pending_req=new LinkedList<req>();
	public static void main(final String[]args)throws Throwable{
//		System.out.println(hello);
		if(!class_init(b.class,args))return;
		resources_lastmod=System.currentTimeMillis();

		if(print_conf_at_startup){
//			print_hr(out,64);
//			try{out.println(InetAddress.getLocalHost());}catch(Throwable ignored){}
			print_hr(out,64);
//			out.println(b.class);
//			print_hr(out,64);
			class_init(b.class,new String[]{"-1"});
			print_hr(out,64);
		}
		if(print_stats_at_startup)stats_to(out);

		final ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		final InetSocketAddress isa=new InetSocketAddress(Integer.parseInt(server_port));
		final ServerSocket ss=ssc.socket();
		ss.bind(isa,max_pending_connections);
		req.init_static();
		if(thd_watch)new thdwatch().start();
		else stats_to(out);
		final Selector sel=Selector.open();
		ssc.register(sel,SelectionKey.OP_ACCEPT);
		Runtime.getRuntime().addShutdownHook(new jvmsdh());
		while(true)try{
//			sel.select(1000);
			sel.select();
			thdwatch.iokeys=sel.keys().size();
			final Iterator<SelectionKey>it=sel.selectedKeys().iterator();
			if(!it.hasNext())continue;
			thdwatch.iosel++;
			while(it.hasNext()){
				thdwatch.ioevent++;
				final SelectionKey sk=it.next();
				it.remove();
				if(sk.isAcceptable()){
					thdwatch.iocon++;
					final req r=new req();
					r.sockch=ssc.accept();
					r.sockch.configureBlocking(false);
					if(tcpnodelay)r.sockch.setOption(StandardSocketOptions.TCP_NODELAY,true);
					r.selkey=r.sockch.register(sel,0,r);
					read(r);
					continue;
				}
				sk.interestOps(0);
				final req r=(req)sk.attachment();
				if(sk.isReadable()){thdwatch.ioread++;read(r);continue;}
				if(sk.isWritable()){thdwatch.iowrite++;write(r);continue;}
				throw new IllegalStateException();
			}}catch(final Throwable e){
				log(e);
			}
	}
	private static void print_hr(final OutputStream os,final int width_in_chars)throws IOException{
//		for(int i=0;i<width_in_chars;i++)
//			os.write((byte)(Math.random()<.5?'~':' '));
		float prob=1;
		float dprob_di=prob/width_in_chars;
		for(int i=0;i<width_in_chars;i++){
			os.write((byte)(Math.random()<prob?'~':' '));
			prob-=dprob_di;
		}
		os.write((byte)'\n');
	}
	private static void read(final req r)throws Throwable{
		if(r.is_sock()){
			if(r.is_sock_thread()){
				r.set_waiting_sock_thread_read();
				thread(r);
				return;
			}
			switch(r.sockread()){default:throw new Error();
			case read:r.selkey.interestOps(SelectionKey.OP_READ);return;
			case write:r.selkey.interestOps(SelectionKey.OP_WRITE);return;
			case close:r.close();thdwatch.socks--;return;
			case wait:r.selkey.interestOps(0);return;
			case noop:return;
			}
		}
		while(true)switch(r.parse()){default:throw new Error();
			case read:r.selkey.interestOps(SelectionKey.OP_READ);return;
			case write:r.selkey.interestOps(SelectionKey.OP_WRITE);return;
			case noop:return;
		}
	}
	private static void write(final req r)throws Throwable{
		if(r.is_sock()){
			if(r.is_sock_thread()){
				r.set_waiting_sock_thread_write();
				thread(r);
				return;
			}
			switch(r.sockwrite()){default:throw new Error();
			case read:r.selkey.interestOps(SelectionKey.OP_READ);break;
			case write:r.selkey.interestOps(SelectionKey.OP_WRITE);break;
			case close:r.close();thdwatch.socks--;break;
			}
			return;
		}
		if(r.is_waiting_write()){synchronized(r){r.notify();}return;}
		if(r.is_transfer()){
			if(!r.do_transfer()){r.selkey.interestOps(SelectionKey.OP_WRITE);return;}
			if(!r.is_connection_keepalive()){r.close();return;}
			if(r.is_buf_empty()){r.selkey.interestOps(SelectionKey.OP_READ);return;}
			read(r);//?? bug stackrain
			return;
		}
		if(r.is_waiting_run())
			thread(r);
		throw new Error();
	}
	static void thread(final req r){
		r.selkey.interestOps(0);//? must?
		if(!b.thread_pool||thdreq.all.size()<thread_pool_size){new thdreq(r);return;}
		synchronized(pending_req){pending_req.addLast(r);pending_req.notify();}
	}
	public static int cp(final InputStream in,final OutputStream out) throws IOException{//?. sts
		final byte[]buf=new byte[io_buf_B];
		int n=0;while(true){final int count=in.read(buf);if(count<=0)break;out.write(buf,0,count);n+=count;}
		return n;
	}
	public static int cp(final Reader in,final Writer out,final sts sts)throws Throwable{
		final char[]buf=new char[io_buf_B];
		int n=0;while(true){final int count=in.read(buf);if(count<=0)break;out.write(buf,0,count);n+=count;if(sts!=null)sts.setsts(Long.toString(n));}
		return n;
	}
	public static synchronized void log(final Throwable t){
		Throwable e=t;
		if(t instanceof InvocationTargetException)e=t.getCause();
		while(e.getCause()!=null)e=e.getCause();
		if(!log_client_disconnects){
			if(e instanceof java.nio.channels.CancelledKeyException)return;
			if(e instanceof java.nio.channels.ClosedChannelException)return;
			if(e instanceof java.io.IOException){
				if("Broken pipe".equals(e.getMessage()))return;
				if("Connection reset by peer".equals(e.getMessage()))return;
				if("An existing connection was forcibly closed by the remote host".equals(e.getMessage()))return;
			}
		}
		err.println("\n\n"+b.stacktraceline(e));
	}
	public static path path(){
		return new path(new File(root_dir),true);
	}
	public static path path(final String path){
		ensure_path_ok(path);
		final path p=new path(new File(root_dir,path));//? dont inst path yet
		final String uri=p.uri();
		if(firewall_paths_on)firewall_ensure_path_access(uri);
		return p;
	}
	static void firewall_ensure_path_access(final String uri){
		//. cleanup
//		final path sessionsdir=new path(new File(root_dir,sessions_dir));//? cache
		final String sessionsdiruri=b.file_to_uri(new File(b.root_dir,b.sessions_dir));
		if(!uri.startsWith(sessionsdiruri+"/"))return;
		try{
			final req r=req.get();
			final String sessionid=r.session().id();
			if(uri.startsWith(sessionsdiruri+"/"+sessionid))return;
			throw new SecurityException("session "+sessionid+" cannot access "+uri);
		}catch(ClassCastException ignored){}
		// allow access to any file, sessionid in path is passphrase
	}
//	static path path_ommit_firewall_check(final String path){
//		ensure_path_ok(path);
//		return new path(new File(root_dir,path));
//	}
	private static void ensure_path_ok(final String path) throws Error{
		if(path.contains(".."))throw new Error("illegalpath "+path+": containing '..'");
	}
	static LinkedList<req>pendingreqls(){return pending_req;}

	private static long stats_last_t_ms;
	private static long stats_last_io_B;
	public static void stats_to(final OutputStream out)throws Throwable{
		final long t_ms=System.currentTimeMillis();
		final long dt_ms=t_ms-stats_last_t_ms;
		stats_last_t_ms=t_ms;
		final long total_io_B=thdwatch.input+thdwatch.output;
		final long dB=total_io_B-stats_last_io_B;
		stats_last_io_B=total_io_B;
		final float dBdt_s=dt_ms==0?0:dB*1000/dt_ms;
		final int throughput_qty;
		final String throughput_unit;
		if(dBdt_s==0){
			throughput_qty=0;
			throughput_unit="";
		}else if(dBdt_s>M){
			throughput_qty=(int)(dBdt_s/M+0.5f);
			throughput_unit=" MB/s";
		}else if(dBdt_s>K){
			throughput_qty=(int)(dBdt_s/K+0.5f);
			throughput_unit=" KB/s";
		}else{
			throughput_qty=(int)(dBdt_s);
			throughput_unit=" B/s";
		}
		final PrintStream ps=new PrintStream(out);
		ps.println(hello);
		for(final NetworkInterface ni:Collections.list(NetworkInterface.getNetworkInterfaces())){
		    final String nm=ni.getName();
		    if(nm.startsWith("lo"))continue;
			p("              url: ");
	        for(final InetAddress ia:Collections.list(ni.getInetAddresses())){
	        	final String s=ia.getHostAddress();
	        	if(!s.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))continue;
	        	p("http://");p(s);if(!server_port.equals("80")){p(":");p(server_port);}p("/");
	        	break;
	        }
	        p("\n");
		}
		ps.println("             time: "+tolastmodstr(t_ms));
		ps.println("             port: "+server_port);
		ps.println("            input: "+(thdwatch.input>>10)+" KB");
		ps.println("           output: "+(thdwatch.output>>10)+" KB");
		ps.println("       throughput: "+throughput_qty+throughput_unit);
		ps.println("         sessions: "+session.all().size());
		ps.println("        downloads: "+new File(root_dir).getCanonicalPath());
		ps.println("     sessions dir: "+new File(root_dir,sessions_dir).getCanonicalPath());
		ps.println("     cached files: "+(req.cachef_size()>>10)+" KB");
		ps.println("      cached uris: "+(req.cacheu_size()>>10)+" KB");
		ps.println("        classpath: "+System.getProperty("java.class.path"));
		final Runtime rt=Runtime.getRuntime();
		if(gc_before_stats)rt.gc();
		final long m1=rt.totalMemory();
		final long m2=rt.freeMemory();
		ps.println("         ram used: "+((m1-m2)>>10)+" KB");
		ps.println("         ram free: "+(m2>>10)+" KB");
		ps.println("          threads: "+thdreq.all.size());
		ps.println("            cores: "+Runtime.getRuntime().availableProcessors());
		ps.println("            cloud: "+cloud_bees);
		ps.println("               id: "+id);
	}
	public static int rndint(final int from,final int tonotincl){return (int)(Math.random()*(tonotincl-from)+from);}
	public static String stacktrace(final Throwable e){final StringWriter sw=new StringWriter();final PrintWriter out=new PrintWriter(sw);e.printStackTrace(out);out.close();return sw.toString();}
	public static String stacktraceline(final Throwable e){return stacktrace(e).replace('\n',' ').replace('\r',' ').replaceAll("\\s+"," ").replaceAll(" at "," @ ");}
	public static String tolastmodstr(final long t){final SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");sdf.setTimeZone(TimeZone.getTimeZone("GMT"));return sdf.format(new Date(t));}
	public static String urldecode(final String s){try{return URLDecoder.decode(s,strenc);}catch(UnsupportedEncodingException e){throw new Error(e);}}
	public static String urlencode(final String s){try{return URLEncoder.encode(s,strenc);}catch(UnsupportedEncodingException e){throw new Error(e);}}
	public static String tostr(final Object object,final String def){return object==null?def:object.toString();}
	public static byte[]tobytes(final String v){try{return v.getBytes(strenc);}catch(UnsupportedEncodingException e){throw new Error(e);}}
	public static String sessionhref(final String sessionid){return sessions_dir+"/"+sessionid+"/";}
	public static boolean isempty(final String s){return s==null||s.length()==0;}
	public static String isempty(final String o,final String def){return isempty(o)?def:o;}
	public static Set<String>sessionsids(){return Collections.unmodifiableSet(session.all().keySet());}//?
	public static long get_session_bits_for_sessionid(final String sesid){//?
		//? file(system){sha1(sessionid),bits}
		if("".equals(sesid))return 0;
		return 1;
	}
	public static void class_printopts(final Class<?>cls)throws IllegalArgumentException,IllegalAccessException{
		for(final Field f:cls.getFields()){
			final Object o=f.get(null);
			out.print(f.getName());
			out.print("=");
			String type=f.getType().getName();
			if(type.startsWith("java.lang."))type=type.substring("java.lang.".length());
			if(type.startsWith("java.util."))type=type.substring("java.util.".length());
			final boolean isstr=type.equals("String");
			final boolean isbool=type.equals("boolean");
			final boolean isint=type.equals("int");
			final boolean islong=type.equals("long");
			final boolean print_type=!(isstr||isbool||isint||islong);
			if(isstr)out.print("\"");
			if(print_type){out.print(type);out.print("(");}
			out.print(o==null?"":o.toString().replaceAll("\\n","\\\\n"));
//			if(islong)out.print("L");
			if(isstr)out.print("\"");
			if(print_type){out.print(")");}
			out.println();
		}
	}
	public static boolean class_init(final Class<?>cls,final String[]args)throws SecurityException,NoSuchFieldException,IllegalArgumentException,IllegalAccessException{
		if(args==null||args.length==0)return true;
//		System.out.println("args:");
//		for(String s:args)System.out.println(s);
		if("-1".equals(args[0])){class_printopts(cls);return false;}
		for(int i=0;i<args.length;i+=2){
			final String fldnm=args[i];
			final Field fld=cls.getField(fldnm);
			final String val=args[i+1];
			pl("conf "+fldnm+"="+val);
			final Class<?>fldcls=fld.getType();
			if(fldcls.isAssignableFrom(String.class))fld.set(null,val);
			else if(fldcls.isAssignableFrom(int.class))fld.set(null,Integer.parseInt(val));
			else if(fldcls.isAssignableFrom(boolean.class))fld.set(null,"1".equals(val)||"true".equals(val)||"yes".equals(val)||"y".equals(val)?Boolean.TRUE:Boolean.FALSE);
			else if(fldcls.isAssignableFrom(long.class))fld.set(null,Long.parseLong(val));
		}
		return true;
	}
	static enum op{read,write,noop}
	private static String ensure(final String s,final String def){
		if(s==null||s.length()==0)return def;
		return s;
	}
	public static void cp(final InputStream in,final Writer out)throws Throwable{
		cp(new InputStreamReader(in,strenc),out,null);
	}
	public static void pl(final String s){out.print("> ");out.println(s);}
	public static void p(final String s){out.print(s);}
	
	//? safe quantity unit math
	public static @Retention(RetentionPolicy.RUNTIME)@interface unit{String name()default"";}
	public static @Retention(RetentionPolicy.RUNTIME)@interface conf{String note()default"";boolean reboot()default false;}
//	public static @Retention(RetentionPolicy.RUNTIME)@interface conf_reboot{String note()default"";}
	public static @Retention(RetentionPolicy.RUNTIME)@interface ref{}
	
	public static @Retention(RetentionPolicy.RUNTIME)@interface acl{
		long create()default 0;
//		long list()default 0;
//		long peek()default 0;
//		long view()default 0;
//		long append()default 0;
//		long edit()default 0;
//		long rename()default 0;
//		long delete()default 0;
	}
//	public static interface client{bits acl_bits();}
//	public static interface bits{
//		boolean hasany(final bits b);
//		boolean hasall(final bits b);
//		int to_int();
//		long to_long();
//	}

	static void acl_ensure_create(final a e){
		final Class<? extends a>ecls=e.getClass();
		final acl a=ecls.getAnnotation(acl.class);
		if(a==null)return;
		final long bits_c=a.create();
		final req r=req.get();
		final session ses=r.session();
		if(ses.bits_hasany(bits_c))return;
		throw new SecurityException("cannot create item of type "+ecls+" due to acl\n any:  0b"+Long.toBinaryString(ses.bits())+" vs 0b"+Long.toBinaryString(bits_c));
	}
	static void acl_ensure_post(final a e){
		final Class<? extends a>ecls=e.getClass();
		final acl a=ecls.getAnnotation(acl.class);
		if(a==null)return;
		final long bits_c=a.create();
		final req r=req.get();
		final session ses=r.session();
		if(ses.bits_hasany(bits_c))return;
		throw new SecurityException("cannot post to item of type "+ecls+" due to acl\n any:  0b"+Long.toBinaryString(ses.bits())+" vs 0b"+Long.toBinaryString(bits_c));
	}
	public static void firewall_assert_access(final a e){
		final Class<? extends a>cls=e.getClass();
		if(cls.equals(a.class))return;
		final String clsnm=cls.getName();
//		final int i=clsnm.lastIndexOf('.');
//		final String pkgnm=i==-1?"":clsnm.substring(0,i);
//		if(pkgnm.endsWith(".a")&&!req.get().session().bits_hasall(2))throw new Error("firewalled1");
		if(clsnm.startsWith("a.localhost.")&&!req.get().ip().toString().equals("/0:0:0:0:0:0:0:1"))throw new Error("firewalled2");
	}
	public static String file_to_uri(final File f){//? cleanup
		final String u1=f.getPath();
		if(!u1.startsWith(root_dir))throw new SecurityException("path "+u1+" not in root "+root_dir);
		final String u4=u1.substring(root_dir.length());
		final String u2=u4.replace(File.pathSeparatorChar,'/');
		final String u3=u2.replace(' ','+');
		return u3;
	}
	public static session session(final String id){return session.all().get(id);}
	public static String uri_to(final Class<? extends a>cls){return"/"+cls.getName().substring(webobjpkg.length());}
}