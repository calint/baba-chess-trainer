package a;
import static b.b.tobytes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;

import b.a;
import b.a_ajaxsts;
import b.req;
import b.xwriter;
final public class $ extends a{
	static final long serialVersionUID=1;
	public a_ajaxsts ajaxsts;
	public a pgnpth;{pgnpth.set("1.pgn");}
	public a dir;{dir.set("problems-dir");}
	public void to(final xwriter x)throws Throwable{
		x.style(ajaxsts,"position:fixed;bottom:0;right:0");
		ajaxsts.to(x);
		x.style(this,"margin:1em;padding:1em;border:1px dotted blue;background-color:#ffe");
		x.el(this);
		x.p(" generate problems from file ");
		x.style(pgnpth,"border:1px dotted green;padding:.5em");
		x.inptxt(pgnpth,this);

		x.nl();
		x.p(" store in folder ");
		x.style(dir,"border:1px dotted green;padding:.5em");
		x.inptxt(dir,this);
		x.el_();
	}
	
	public void x_(final xwriter y,final String str)throws Throwable{
		final float devthresh=1;
		int i=1;
		final pgnreader pgn=new pgnreader(req.get().session().path(pgnpth.str()).reader());
		final xwriter xpre=new xwriter();
		xpre.p("<!doctype html><meta charset=utf-8>");
		xpre.style();
		try(final InputStream is=$.class.getResourceAsStream("/b/x.css")){
			b.b.cp(is,xpre.outputstream());
		}
		xpre.nl();
		xpre.css("table.chsboard","border:1px solid black");
		xpre.css("table.chsboard td","width:45px;height:45px;align:center;vertical-align:middle");
		xpre.css("table.chsboard td.wht","background:white");
		xpre.css("table.chsboard td.blk","background:#a0a0a0");
		xpre.style_();		
		xpre.style(this,"margin:1em;padding:1em;border:1px dotted blue;background-color:#ffe");
		xpre.el(this);
		final String prestr=xpre.toString();
		
		
		try(final crafty cft=new crafty()){
			while(true) {
				xwriter x=new xwriter();
				x.p(prestr);
				final int res=scan_pgn(x,pgn,cft,devthresh);
				if(res==1)
					return;
				if(res==2)
					continue;
				req.get().session().path("problem"+i+++".html").writestr(x.toString());
			}
		}
	}

	
	public static int scan_pgn(final xwriter x,final pgnreader pgn,final crafty cft,final float devthresh)throws Throwable{
		final Map<String,String>hdr=pgn.next_tags();
		if(hdr==null)
			return 1;
		float devf=0,prvevf=0;
		String cev="";
		boolean found=false;
		cft.reset();
		while(true){
			final String move=pgn.next_move();
			if(move==null)
				return 2;
			cev=cft.move(move);
			final String[]splt=cev.split("\\s+");
			final String ev=splt[1];
			if(ev.contains("Mat"))
				return 2;
			final float evf=Float.parseFloat(ev);
			devf=evf-prvevf;
			if(Math.abs(devf)>devthresh){
				found=true;
				break;
			}
			prvevf=evf;
		}
		if(!found)
			return 2;

		while(true){ // read rest of moves
			final String mv=pgn.next_move();
			if(mv==null)
				break;
		}
		x.spc().p(devf>=0?"white":"black").p(" to move and gain ").p(((float)((int)(Math.abs(devf)*10))/10));
		x.nl();
		
		final xwriter board=new xwriter();
		cft.diagram(board.outputstream());
		final byte[]diagbytes=tobytes(board.toString());
		final Scanner scb=new Scanner(new InputStreamReader(new ByteArrayInputStream(diagbytes)));
		scb.nextLine();
		int lineno=0;
		boolean sqcolwh=true;
		
		x.nl();
		x.table("chsboard");
		while(scb.hasNextLine()){
			final String ln=scb.nextLine();
			if((lineno++%2)==1)continue;
			if(lineno==17)break;
			try(final Scanner scln=new Scanner(ln)){
				scln.findInLine("\\|");
				x.tr();
				for(int i=0;i<8;i++){
					final String cell=scln.findInLine("(\\s{3})|(\\<.*?\\>)|(\\-.*?\\-)|( . )");
					final boolean iswht=cell.substring(0,1).equals("-");
					final String piece=cell.substring(1,2).toLowerCase();
					x.td(sqcolwh?"wht":"blk");
					final String img;
					if(piece.equals(" ")||piece.equals("."))img="";
					else img="<img src=/i/45px-Chess_"+piece+(iswht?"l":"d")+"t45.svg.png>";
					sqcolwh=!sqcolwh;
					x.p(img);
				}
			}
			sqcolwh=!sqcolwh;
		}
		scb.close();
		x.table_();
		x.nl(10);
		try(final Scanner evsc=new Scanner(cev)){
			evsc.next();
			evsc.next();
			x.spc(2).p("hint: ").p(evsc.nextLine().trim());
		}
		return 0;
	}

}
