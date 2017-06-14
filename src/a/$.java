package a;
import b.a;
import b.a_ajaxsts;
import b.req;
import b.xwriter;
final public class $ extends a{
	static final long serialVersionUID=1;
	public a_ajaxsts ajaxsts;
	public a pgn_file;{pgn_file.set("1.pgn");}
	public void to(final xwriter x)throws Throwable{
		x.style(ajaxsts,"position:fixed;bottom:0;right:0");
		ajaxsts.to(x);
		x.style(this,"margin:1em;padding:1em;border:1px dotted blue;background-color:#ffe");
		x.el(this);
		x.p("baba chess trainer on file ");
		x.style(pgn_file,"border:1px dotted green;padding:.5em");
		x.inptxt(pgn_file,this);
		x.nl(2);
		req.get().session().path(pgn_file.str()).to(x);
		x.el_();
	}
}
