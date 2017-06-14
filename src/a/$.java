package a;
import b.a;
import b.a_ajaxsts;
import b.xwriter;
final public class $ extends a{
	static final long serialVersionUID=1;
	public a_ajaxsts ajaxsts;
	public void to(final xwriter x)throws Throwable{
		x.el(this);
		x.style();
		x.css(ajaxsts,"position:fixed;bottom:0;right:0");
		x.css("table.chsboard","border:1px solid black");
		x.css("table.chsboard td","width:45px;height:45px;align:center;vertical-align:middle");
		x.css("table.chsboard td.wht","background:white");
		x.css("table.chsboard td.blk","background:#a0a0a0");
		x.style_();
		ajaxsts.to(x);
		x.pl("baba chess trainer");
		x.el_();
	}
}
