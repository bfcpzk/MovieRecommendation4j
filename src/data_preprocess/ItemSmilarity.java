package data_preprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Movie;
import model.MovieSimilarity;
import util.Jdbc_Util;

public class ItemSmilarity {
	private static String[] category_dict = new String[]{"Action","Adventure","Animation","Children.s","Comedy","Crime","Documentary","Drama","Fantasy","Film-Noir","Horror","Musical","Mystery","Romance","Sci-Fi","Thriller","War","Western"};
	
	private static List<Movie> mlist = new ArrayList<Movie>();
	
	private static Map<Movie,int[]> movie_map = new HashMap<Movie, int[]>();
	
	private static List<MovieSimilarity> mslist = new ArrayList<MovieSimilarity>();
	
	public static void main(String[] args) throws SQLException{
		Jdbc_Util db = new Jdbc_Util();
		String sql = "select * from movie_meta_data";
		ResultSet rs;
		rs = db.select(sql);
		System.out.println("初始化movie实体列表");
		//初始化movie实体列表
		while(rs.next()){
			Movie m = new Movie();
			m.setId(rs.getInt("id"));
			m.setMovie_name(rs.getString("movie_name"));
			m.setMovie_category(rs.getString("movie_catagory"));
			mlist.add(m);
		}
		System.out.println(mlist.size());
		System.out.println("数据结构转化，预处理category类型");
		//数据结构转化，预处理category类型
		for(int i = 0 ; i < mlist.size() ; i++){
			Movie m = new Movie();
			m = mlist.get(i);
			String[] temp;
			temp = m.getMovie_category().split("&");
			//System.out.println(temp[0]);
			int[] cate = new int[category_dict.length];
			for(int j = 0 ; j < temp.length ; j++){
				for(int k = 0 ; k < category_dict.length ; k++){
					if(temp[j].equals(category_dict[k])){
						cate[k] = 1;
					}
				}
			}
			movie_map.put(m, cate);
			//System.out.println(cate);
		}
		System.out.println(movie_map.keySet().size());
		System.out.println("计算电影相似度");
		//计算电影相似度
		for(Movie keyA : movie_map.keySet()){
			for(Movie keyB : movie_map.keySet()){
				if(keyA.getId() != keyB.getId()){//不是同一部电影才计算相似度
					int[] tempA = movie_map.get(keyA);
					int[] tempB = movie_map.get(keyB);
					int count = 0;
					for(int i = 0 ; i < category_dict.length ; i++){
						if(tempA[i] == 1 && tempB[i] == 1){
							count++;
						}
					}
					if(count != 0){
						MovieSimilarity ms = new MovieSimilarity();
						ms.setMaid(keyA.getId());
						ms.setMbid(keyB.getId());
						ms.setSim_score(count);
						mslist.add(ms);				
					}
				}
			}
		}
		System.out.println(mslist.size());
		System.out.println("相似度矩阵入库");
		//相似度矩阵入库
		for(int i = 0 ; i < mslist.size() ; i++){
			sql = "insert into `ItemSimilarity` (`aid`, `bid`, `score`) values ('" + mslist.get(i).getMaid() + "', '" + mslist.get(i).getMbid() + "', '" + mslist.get(i).getSim_score() + "')";
			db.add(sql);
		}
	}
}
