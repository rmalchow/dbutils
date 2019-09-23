import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class IpCombine {

	
	public static void main(String[] args) throws IOException {
		List<String> ranges = new ArrayList<>();
		for(File f : new File(".").listFiles()) {
			if(!f.isFile()) continue;
			if(!f.getName().contains("EC2")) continue;
			InputStream is = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String s = "";
			while((s = br.readLine())!=null) {
				if(!s.contains("/")) continue;
				String[] x = s.split("/")[0].split("\\.");
				List<String> g = new ArrayList<String>();
				for(String h : x) {
					if(h.length()==1) g.add("00"+h);
					if(h.length()==2) g.add("0"+h);
					if(h.length()==3) g.add(""+h);
				}
				
				ranges.add(StringUtils.join(g,".")+"/"+s.split("/")[1]);
			}
			
			
			br.close();
			is.close();
		}

		List<Range> ran = new ArrayList<>();
		
		for(String s : ranges) {
			System.err.println("--- "+s+"----");
			String[] a = s.split("/");
			String[] b = a[0].split("\\.");
			int range = Integer.parseInt(a[1]);
			long from = 0;
			for(String c : b) {
				from = from << 8 | Integer.parseInt(c);
			}

			long to = from;
			
			for(int i=0; i<(32-range); i++) {
				to = to | (1<<i);
			}
		
			Range r = new Range();
			r.setStart(from);
			r.setEnd(to);
			r.setMask(range);

			ran.add(r);
			
		}
		
		Collections.sort(ran);
		
		List<Range> o = new ArrayList<Range>();
		
		System.err.println(ran.size());
		
		while (ran.size()>0) {
			Range r = ran.remove(0);
			Range r2 = r.expand(3);
			boolean combined = false;
			System.err.println(r);
			while(ran.size()>0 && ran.get(0).within(r2)) {
				System.err.println("  + "+ran.remove(0));
				
				combined = true;
			}
			if(combined) {
				System.err.println(" ---> "+r2);
				o.add(r2);
			} else {
				System.err.println(" ---> "+r);
				o.add(r);
			}
		}
		System.err.println(o.size());
		
		for(Range r : o) {
			System.err.print (r+" ");
		}

		
	}
	
	
}
