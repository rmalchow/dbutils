import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Range implements Comparable<Range> {
	
	private long start;
	private long end;
	private int mask;
	
	
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	public int getMask() {
		return mask;
	}
	public void setMask(int mask) {
		this.mask = mask;
	}

	public Range expand(int distance) {
		Range r = new Range();
		long end = getEnd();
		int mask = getMask() - distance;
		for(int i=0; i<(32-(getMask()-distance)); i++) {
			end = end | (1<<i);
		}
		r.setEnd(end);
		r.setMask(mask);
		r.setStart(start);
		return r;
	}
	
	public boolean within(Range range) {
		if(range.getStart()>getStart()) return false;
		if(range.getEnd()<getEnd()) return false;
		return true;
	}
	
	@Override
	public int compareTo(Range arg0) {
		return new Long(getStart()).compareTo(new Long(arg0.getStart()));
	}
	
	@Override
	public String toString() {
		List<String> s = new ArrayList<String>();
		for(int i=3;i>=0;i--) {
			s.add(""+(start>>(8*i) & 0xFF));
		}
		return StringUtils.join(s,".")+"/"+getMask();
	}
	
	
}