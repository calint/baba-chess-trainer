package a;
import b.a;
import b.a_ajaxsts;
import b.xwriter;
final public class $ extends a{
	static final long serialVersionUID=1;
	public a_ajaxsts ajaxsts;
	public a pgn_file;{pgn_file.set("1.pgn");}
	public void to(final xwriter x)throws Throwable{
		x.style(ajaxsts,"position:fixed;bottom:0;right:0");
		ajaxsts.to(x);
		x.nl(2);
		
		x.style(this,"margin:3em;padding:3em;border:1px dotted red;background-color:lightblue").nl();
		x.el(this);
		x.p("baba chess trainer on file ");
		x.style(pgn_file,"border:1px dotted green;padding:.5em");
		x.inptxt(pgn_file,this);
		x.pl("");
		x.el_();
	}
}
