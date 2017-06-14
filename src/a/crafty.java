package a;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import b.b.conf;

final public class crafty implements AutoCloseable{
	@conf public static String crafty_path="crafty";
	public int srchdpth=10;
	private final Process p;
	private final OutputStream os;
	private final InputStream is;
	private final Scanner sc;
	public crafty(){try{
		final ProcessBuilder pb=new ProcessBuilder(crafty_path);
		pb.directory(new File("."));
		pb.redirectErrorStream(true);
		p=pb.start();
		is=p.getInputStream();
		os=p.getOutputStream();
		os.write(("book off\nlog off\nsd "+srchdpth+"\nanalyze\n").getBytes());
		os.flush();
		sc=new Scanner(is);
		sc.findWithinHorizon("analyze\\.White\\(1\\): ",0);
	}catch(final Throwable t){throw new Error(t);}}
	public void reset()throws IOException{
		os.write("reset 1\n".getBytes());
		os.flush();
		scantillnextinput();			
	}
	public String move(final String mv)throws Throwable{
		os.write((mv+"\n").getBytes());
		os.flush();
		sc.findWithinHorizon("->",0);
		final String ln=sc.nextLine().trim();
		scantillnextinput();
		return ln;
	}
	public void back()throws Throwable{
		os.write("\n".getBytes());
		os.flush();
		scantillnextinput();
	}
	public void diagram(final OutputStream out)throws Throwable{
		final PrintWriter pw=new PrintWriter(out,true);
		os.write("d\n".getBytes());
		os.flush();
		sc.nextLine();
		sc.nextLine();
		for(int i=0;i<8*2+2;i++){
			final String s1=sc.nextLine();
			pw.println(s1);
		}
		scantillnextinput();
	}
	private void scantillnextinput(){
		sc.findWithinHorizon("analyze\\.(White|Black)\\(\\d+\\): ",0);
	}
	public void close(){try{
//		os.write("q\n".getBytes());
//		os.flush();
		p.destroy();
	}catch(Throwable t){throw new Error(t);}}
}