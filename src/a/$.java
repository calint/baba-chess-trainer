package a;
import static b.b.tobytes;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;
import b.a;
import b.a_ajaxsts;
import b.xwriter;
final public class $ extends a{
	static final long serialVersionUID=1;
	public a sts;
	public a dsp;
	public a diag;
	public a grph;
	public a hint;
	public a in1;//pgn input
	public a devthr;{devthr.set(.8f);}
	public a crafty_bin_path;{crafty_bin_path.set("crafty");}
	public ficsgames fg;
	public a_ajaxsts ajaxsts;
	public void to(final xwriter x)throws Throwable{
		x.el(this);
//		x.el(this,"width:30em;color:#222;margin-left:auto;margin-right:auto;padding:0 4em 0 4em;display:block;border-right:0px dotted #666;border-left:0px dotted #666;box-shadow:0 0 17px rgba(0,0,0,.5);border-radius:1px");
//		x.el(this);
		x.style();
//		x.css(this,"width:768px;color:#222;margin-left:auto;margin-right:auto;padding:0 4em 0 4em;display:block;border-right:0px dotted #666;border-left:0px dotted #666;box-shadow:0 0 17px rgba(0,0,0,.5);border-radius:1px");
//		x.css(this,"margin:0 4em 0 4em;padding:0 4em 0 4em;box-shadow:0 0 17px rgba(0,0,0,.5);border-radius:1px");
//		x.css("html","padding-left:2em;font-size:1em");
		x.css(sts,"color:green");
		x.css(in1,"border:1px dotted green;width:100%;height:8em");
//		x.css(dsp,"border:1px dotted blue;display:block");
//		x.css(grph,"display:block");
		x.css(diag,"display:block");
		x.css(hint,"display:block;margin-top:1em;border:1px dotted yellow;background:#f0fff0;margin:1em;padding:1em;break:left");
		x.css("table.chsboard","border:1px solid black");
		x.css("table.chsboard td","width:45px;height:45px;align:center;vertical-align:middle");
		x.css("table.chsboard td.wht","background:white");
		x.css("table.chsboard td.blk","background:#a0a0a0");
		x.css(devthr,"border:1px dotted green;text-align:right;width:2em;padding:.5em;background:yellow");
		x.style_();
//		x.pre();
		x.style(ajaxsts,"position:fixed;bottom:0;right:0");
		ajaxsts.to(x);
		x.nl();
		fg.to(x);
		x.nl(11);
//		x.output(sts).nl();
//		x.table("margin-left:auto;margin-right:auto",null).tr().td();
		x.output_holder(grph).nl().output_holder(sts).output_holder(dsp).nl(2);
		x.tag("figure");
		x.span(diag);
		x.tag("figcaption");
		x.output_holder(hint);
		x.tage("figcaption");
		x.tage("figure");
//		x.tableEnd();
		x.nl();
		x.pl("             solved             try again later");
		x.nl(1);
		x.spc(5).pl("<a href=javascript:$('"+ajaxsts.id()+"').scrollIntoView(true)>to top of page</a>");
		x.nl(23);
		
		x.nl(2);
		x.p("paste pgn below").nl().inptxtarea(in1);
		x.ax(this,null,"•·scan").p(" for blunders using threshhold ").inpflt(devthr).nl();
		x.nl(2).spc(7).pl(" <a href=javascript:$('"+ajaxsts.id()+"').scrollIntoView(true)>to top of page</a>");
		x.nl(13);
		x.p("config:\n   crafty bin path: ").inp(crafty_bin_path,"text","border:1px solid #eee;width:15em",null,null,null,null,null,null);
		x.nl(7);
		x.el_();
	}
	public void x_(final xwriter x,final String s)throws Throwable{
		final pgnreader pgn=new pgnreader(in1.toString());
		final Map<String,String>hdr=pgn.next_tags();
		if(hdr==null)throw new Exception("no pgn to parse");
		x.xu(sts,"");
		x.xu(dsp,"");
		x.xu(diag,"");
		x.xu(hint,"");
		x.xu(grph,"");
//		x.xfocus(sts);
		final xwriter xds=new xwriter();
		float prvevf=0;
//		String prvcev="";
//		String prvmove="";
		crafty.crafty_path=crafty_bin_path.str();
		try(final crafty cft=new crafty()){//? optional construct for custom path
			cft.reset();
			int ply=0;
			boolean found=false;
			final float devthresh=devthr.toflt();
			x.pl("$('"+grph.id()+"').scrollIntoView(true);");
			while(true){
				final String move=pgn.next_move();
				if(move==null)break;
				ply++;
				if(ply%2==1)xds.p((ply>>1)+1).p(". ");
				xds.p(move).spc();
				if(ply%2!=1)xds.spc();
				final String cev=cft.move(move);
				x.xu(sts,ply+". "+move+": "+cev).flush();
				final String[]splt=cev.split("\\s+");
				final String ev=splt[1];
				if((ply%2)==1)x.xp(grph,((ply/2)+1)+". ");
				x.xp(grph,move+" ("+ev+") ");
				if((ply%2)!=1)x.xp(grph,"\n");
				if(ev.contains("Mat")){
					x.xalert("no blunder at threshold "+devthr);//x.xu(devthr.set(devthr.toflt()/2));
					x.xfocus(devthr);
					return;
				}
				final float evf=Float.parseFloat(ev);
				final float devf=evf-prvevf;
				if(Math.abs(devf)>devthresh){
					x.xu(dsp," "+(devf>=0?"white":"black")+" to move and gain "+((float)((int)(Math.abs(devf)*10))/10));
					final xwriter board=new xwriter();
					cft.diagram(board.outputstream());
					final byte[]diagbytes=tobytes(board.toString());
					final Scanner scb=new Scanner(new InputStreamReader(new ByteArrayInputStream(diagbytes)));
					scb.nextLine();
					int lineno=0;
					boolean sqcolwh=true;
					final xwriter dg=new xwriter();
					dg.table("chsboard");
					while(scb.hasNextLine()){
						final String ln=scb.nextLine();
						if((lineno++%2)==1)continue;
						if(lineno==17)break;
						try(final Scanner scln=new Scanner(ln)){
							scln.findInLine("\\|");
							dg.tr();
							for(int i=0;i<8;i++){
								final String cell=scln.findInLine("(\\s{3})|(\\<.*?\\>)|(\\-.*?\\-)|( . )");
								final boolean iswht=cell.substring(0,1).equals("-");
								final String piece=cell.substring(1,2).toLowerCase();
								dg.td(sqcolwh?"wht":"blk");
								final String img;
								if(piece.equals(" ")||piece.equals("."))img="";
								else img="<img src=/chs/45px-Chess_"+piece+(iswht?"l":"d")+"t45.svg.png>";
								sqcolwh=!sqcolwh;
								dg.p(img);
							}
						}
						sqcolwh=!sqcolwh;
					}
					scb.close();
					dg.table_();
					x.xu(diag,dg.toString());
					final Scanner evsc=new Scanner(cev);
					evsc.next("\\-?\\d+\\.\\d+");
					evsc.next("\\-?\\d+\\.\\d+");
					x.xu(hint,"  hint: "+evsc.nextLine().trim());
					evsc.close();
					xds.flush();
					found=true;
					break;
				}
				prvevf=evf;
	//			prvcev=cev;
	//			prvmove=move;
			}
			x.xu(sts,"");
			if(!found){
				x.xu(dsp," no blunders found at evaluation threshhold "+devthresh+" with search depth "+cft.srchdpth+" ply");
				return;
			}
		}
	}
	
	
	@Override protected void ev(xwriter x,a from,Object o)throws Throwable{
		if(from==fg){
			in1.set(o.toString());
			x.xu(in1);
			x_(x,null);
			return;
		}
		super.ev(x,from,o);
	}
}
