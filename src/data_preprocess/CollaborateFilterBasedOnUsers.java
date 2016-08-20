package data_preprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import util.Jdbc_Util;
import model.Score;
import model.User;

public class CollaborateFilterBasedOnUsers {
	
	private static Map<Integer,List<Score>> usMap = new HashMap<Integer,List<Score>>();//用户打分列表
	
	private static Map<Double,Integer> similarUserIdScoreMap = new TreeMap<Double,Integer>().descendingMap();//与目标用户的相似用户列表
	
	private static int recommendNum = 10;//推荐的电影数
	
	private static Set<Integer> recommendSet = new HashSet<Integer>();//待推荐的电影列表
	
	private static Map<Double, Integer> recommendResultMap = new TreeMap<Double, Integer>().descendingMap();//推荐分值,电影ID
	
	public static double calSimilarInterest(int sa, int sb){
		return (double)(1/(Math.abs(sa-sb)+1));
	}
	
	
	public static void main(String[] args) throws SQLException{
		Jdbc_Util db = new Jdbc_Util();
		Scanner in = new Scanner(System.in);
		System.out.println("请输入要为哪个用户推荐(用户ID)：");
		String line = in.nextLine();
		in.close();
		
		//初始化usMap
		String sqlUser = "select * from user_meta_data";
		ResultSet rs;
		rs = db.select(sqlUser);
		List<User> ulist = new ArrayList<User>();
		while(rs.next()){
			User user = new User();
			user.setId(rs.getInt("id"));
			ulist.add(user);
		}
		for(int i = 0 ; i < ulist.size() ; i++){
			String tempsql = "select * from rating_meta_data where uid='" + ulist.get(i).getId() + "'";
			rs = db.select(tempsql);
			List<Score> slist = new ArrayList<Score>();
			while(rs.next()){
				Score s = new Score();
				s.setUid(ulist.get(i).getId());
				s.setMid(rs.getInt("mid"));
				s.setRating(rs.getInt("rating"));
				s.setTimestamp(rs.getString("timestamp"));
				slist.add(s);
			}
			usMap.put(ulist.get(i).getId(), slist);
		}
		
		//遍历usMap寻找相似的用户
		List<Score> tulist = usMap.get(Integer.parseInt(line));
		for(int key : usMap.keySet()){//遍历每一个非目标推荐用户
			if(key != Integer.parseInt(line)){//不是目标推荐用户
				List<Score> s_sb_list = usMap.get(key);
				double count = 0.0;
				for(int a = 0 ; a < tulist.size() ; a++){
					for(int b = 0 ; b < s_sb_list.size() ; b++){
						if(tulist.get(a).getMid() == s_sb_list.get(b).getMid()){//用户b与用户a观看了同一部电影
							count += calSimilarInterest(tulist.get(a).getRating(),s_sb_list.get(b).getRating());
						}
					}
				}
				double simScore_a_b = count/(tulist.size() * s_sb_list.size());
				similarUserIdScoreMap.put(simScore_a_b, key);
			}
		}
		
		//构建全部电影集
		Set<Integer> allMovieList = new HashSet<Integer>();
		int count = 0;
		for(Entry e : similarUserIdScoreMap.entrySet()){//遍历最相近的10个用户
			if(count >= recommendNum){
				break;
			}
			int uid = (int)e.getValue();
			List<Score> stemp = usMap.get(uid);
			for(int i = 0 ; i < stemp.size() ; i++){
				allMovieList.add(stemp.get(i).getMid());
			}
			count++;
		}
		//除去已观看
		Set<Integer> haveSeen = new HashSet<Integer>();
		List<Score> targetList = usMap.get(Integer.parseInt(line));
		for(int i = 0 ; i < targetList.size() ; i++){
			haveSeen.add(targetList.get(i).getMid());
		}
		//集合做差
		recommendSet.addAll(allMovieList);
		recommendSet.retainAll(haveSeen);
		
		//构建推荐列表
		for(int mid: recommendSet){//对于待推荐的每一部电影计算分值
			double mid_recom_score = 0.0;
			count = 0;
			for(Entry e : similarUserIdScoreMap.entrySet()){//对于前十个最相近的用户计算
				if(count >= 10){
					break;
				}
				int uid = (int)e.getValue();//选出相似用户中的一个
				double simScore = (double)e.getKey();//选出当前用户的相似度
				List<Score> stemp = usMap.get(uid);
				for(int k = 0 ; k < stemp.size() ; k++){//检查该相似用户是否看过该电影
					if(mid == stemp.get(k).getMid()){//相似用户确实看过该电影
						mid_recom_score += stemp.get(k).getRating() * simScore;
					}
				}
				count++;
			}
			recommendResultMap.put(mid_recom_score,mid);
		}
		
		//展示推荐结果
		String sql;
		count = 0;
		System.out.println("推荐列表：");
		System.out.println("电影名称" + "      推荐分值");
		for(Entry e : recommendResultMap.entrySet()){
			if(count >= 10){
				break;
			}
			sql = "select movie_name from movie_meta_data where id='" + e.getValue() + "'";
			rs = db.select(sql);
			while(rs.next()){
				System.out.println(rs.getString("movie_name") + " " + e.getKey());
			}
			count++;
		}
	}
}
