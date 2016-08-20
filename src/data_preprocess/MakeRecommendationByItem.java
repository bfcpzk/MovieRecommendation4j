package data_preprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import util.Jdbc_Util;

public class MakeRecommendationByItem {
	private static List<Integer> recommendListMovieId = new ArrayList<Integer>();
		
	public static void main(String[] args) throws SQLException{
		Jdbc_Util db = new Jdbc_Util();
		Scanner in = new Scanner(System.in);
		System.out.println("请输入该用户最近观看的一部电影id：");
		String targetId = in.nextLine();
		String sql = "select * from ItemSimilarity where aid='" + targetId + "' order by score desc";
		ResultSet rs;
		rs = db.select(sql);
		int n = 1;
		while(rs.next()){
			if(n > 10){
				break;
			}
			Integer temp = rs.getInt("bid");
			n++;
			recommendListMovieId.add(temp);
		}
		String rsql = "select movie_name,movie_catagory from movie_meta_data where id='" + targetId + "'";
		rs = db.select(rsql);
		while(rs.next()){
			System.out.println(targetId + " " + rs.getString("movie_name") + " " + rs.getString("movie_catagory"));			
		}
		System.out.println("推荐列表：");
		for(int i = 0 ; i < recommendListMovieId.size() ; i++){
			String resultsql = "select movie_name,movie_catagory from movie_meta_data where id='" + recommendListMovieId.get(i) + "'";
			rs = db.select(resultsql);
			while(rs.next()){
				System.out.println(rs.getString("movie_name") + " " + rs.getString("movie_catagory"));
			}
		}
		in.close();
	}
}
