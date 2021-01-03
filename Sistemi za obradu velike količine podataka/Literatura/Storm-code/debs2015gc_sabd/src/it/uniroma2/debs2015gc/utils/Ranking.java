package it.uniroma2.debs2015gc.utils;

import java.io.Serializable;
import java.util.List;

public class Ranking implements Serializable {

	private static final long serialVersionUID = 1L;

	List<RankItem> ranking;
	
	public Ranking() {
	
	}

	public List<RankItem> getRanking() {
		return ranking;
	}

	public void setRanking(List<RankItem> ranking) {
		this.ranking = ranking;
	}
	
}
