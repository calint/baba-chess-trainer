package a;

import static b.b.K;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import b.a;
import b.b;
import b.path;
import b.req;
import b.xwriter;
import c.client;
import c.conn;

final public class ficsgames extends a{
	public a pl;{pl.set("fics handle");}//player name
	public a af;//query date
	public a ot;//output
	public ficsgames(){
		final String s=DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
		af.set(s);
	}
	@Override public void to(xwriter x)throws Throwable{
		x.el(this);
		x.inp(pl,"text","width:6em;padding:.5em;background:yellow;border:1px dotted gray",null,this,"",null,null,null);
		x.spc();
		x.inp(af,"date","width:10em;padding:.5em;background:yellow;border:1px dotted gray",null,this,"",null,null,null);
		x.spc(8).ax(this,"","•..•").nl();
		x.spanx(ot);
		x.el_();
	}

	public void x_(xwriter y,String s)throws Throwable{
		final xwriter x=y.xub(ot,true,false);
		final pgnreader ps=query(pl.str(),af.str());
		while(true){
//			x.nl().nl();
			final Map<String,String>tags=ps.next_tags();
			if(tags==null)break;
//			x.pl(tags.toString());
			x.el("border-bottom:1px dotted gray;display:block;padding:1em;font-size:1em");
			x.p(tags.get("Date")).spc().p(tags.get("Time")).spc(3);
			String mv=ps.next_move();
			final xwriter z=new xwriter();
			while(mv!=null){
				z.p(mv).spc();
				mv=ps.next_move();
			}
			final String moves=z.toString();
			final int chars_per_line=60;
			final String link_text=moves.length()>chars_per_line?moves.substring(0,chars_per_line):moves;
			x.ax(this,"e "+z,moves,link_text);
			x.el_();
		}
		y.xube();
	}
	public void x_e(xwriter x,String s)throws Throwable{
		ev(x,this,s);
	}	
//	http://ficsgames.org/cgi-bin/search.cgi?player=ctenitchi&action=Finger

	public pgnreader query(final String player_name,final String after_date)throws Throwable{
		//	http://ficsgames.org/cgi-bin/search.cgi?white=ctenitchi&colors=1&date-sel-after=2013&date-sel-after-mm=07&date-sel-after-dd=04&dlgamesnomtimes=Download+(no+movetimes)
		if(player_name==null||player_name.isEmpty())throw new Error("must enter playername");
		final String[]ad=after_date.split("\\-");
		try(final client c=new client("ficsgames.org",80)){
			final StringBuilder sb=new StringBuilder();
			sb.append("/cgi-bin/search.cgi?white=").append(player_name).append("&date-sel-after=").append(ad[0]).append("&date-sel-after-mm=").append(ad[1]).append("&date-sel-after-dd=").append(ad[2]).append("&colors=1&dlgamesnomtimes=Download+(no+movetimes)");
			final String uri=sb.toString();
			
			final ByteArrayOutputStream bos=new ByteArrayOutputStream(8*K);//? buffer in file, pipe
//			try{c.get(uri,bos::write,c::close);}catch(Throwable t){t.printStackTrace();}
			try{c.get(uri,
					new conn.oncontent(){@Override public void oncontent(byte[]data,int offset,int len)throws Throwable {
						bos.write(data,offset,len);
					}},new conn.ongetdone(){@Override public void ongetdone()throws Throwable{
						c.close();
					}}
				);
			}catch(Throwable t){t.printStackTrace();}
			c.run();
			
			final byte[]ba=bos.toByteArray();
			try(final ZipInputStream zis=new ZipInputStream(new ByteArrayInputStream(ba))){
				final ZipEntry ze=zis.getNextEntry();
				final path p=req.get().session().path(getClass()).get(uri);
				try(final OutputStream pos=p.outputstream()){b.cp(zis,pos);}
//				zis.close();
				return new pgnreader(p.reader());
			}
		}
	}

	private static final long serialVersionUID=1L;
}
