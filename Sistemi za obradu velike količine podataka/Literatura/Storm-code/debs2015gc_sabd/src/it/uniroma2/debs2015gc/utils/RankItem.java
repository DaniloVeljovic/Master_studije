package it.uniroma2.debs2015gc.utils;

import java.io.Serializable;

public class RankItem implements Serializable{

	private static final long serialVersionUID = 1L;

	private String route; 
	private int frequency;
	private long timestamp;
	
	public RankItem() {
	}
	
	public RankItem(String route, int frequency, long timestamp) {
		super();
		this.route = route;
		this.frequency = frequency;
		this.timestamp = timestamp;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	} 
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public boolean equals(Object obj) {
	
		if (obj == null || !(obj instanceof RankItem))
			return false;
		
		RankItem other = (RankItem) obj;
		
		if (this.route.equals(other.route))
			return true;
		
		return false;
	}
	
	@Override
	public String toString() {
		return route + ":" + String.valueOf(frequency);
	}
}
