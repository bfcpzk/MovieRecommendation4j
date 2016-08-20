package data_preprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class MakeRecommendationByUsers {
		
	private static int simUserNum = 10;
	
	private static double[] parameter = new double[]{0.3,0.4,0.3};
	
	private static Map<Integer,List<Score>> simUserScoring = new HashMap<Integer, List<Score>>();//相似用户的电影评分纪录
	
	private static Set<Integer> recommendMovieSet = new HashSet<Integer>();//待推荐的电影id集合，相似用户看过的，目标用户未看的
	
	private static Map<Integer, Double> recommendResultList = new TreeMap<Integer, Double>();
	
	
	public static void main(String[] args) throws SQLException{
		Jdbc_Util db = new Jdbc_Util();
		Scanner in = new Scanner(System.in);
		System.out.println("请输入要进行推荐的用户ID:");
		String line = in.nextLine();
		
		//初始化用户集
		String sql = "select * from user_meta_data where id!='" + line + "'";
		ResultSet rs;
		rs = db.select(sql);
		List<User> ulist = new ArrayList<User>();
		while(rs.next()){
			User u = new User();
			u.setId(rs.getInt("id"));
			u.setAge(rs.getString("age"));
			u.setGender(rs.getString("gender"));
			u.setWork(rs.getString("work"));
			ulist.add(u);
		}
		
		//初始化推荐目标用户
		sql = "select * from user_meta_data where id='" + line + "'";
		rs = db.select(sql);
		User tarUser = new User();
		while(rs.next()){
			tarUser.setId(rs.getInt("id"));
			tarUser.setAge(rs.getString("age"));
			tarUser.setGender(rs.getString("gender"));
			tarUser.setWork(rs.getString("work"));
		}
		
		Map<Integer, Double> simUsers = new TreeMap<Integer,Double>();//用户id，与目标用户的相似度得分
		
		//计算用户相似度
		double simscore;
		for(int i = 0 ; i < ulist.size() ; i++){
			simscore = 0.0;
			User utemp = ulist.get(i);
			if(utemp.getGender().equals(tarUser.getGender())){//性别相同
				simscore += parameter[0] * 1;
			}
			if(utemp.getWork().equals(tarUser.getWork())){//职业相同
				simscore += parameter[2] * 1;
			}
			simscore += (double)(1/(Math.abs(Integer.parseInt(utemp.getAge()) - Integer.parseInt(tarUser.getAge())) + 1));
			simUsers.put(utemp.getId(), simscore);
		}
		//这里将map.entrySet()转换成list
        List<Map.Entry<Integer,Double>> simUsersList = new ArrayList<Map.Entry<Integer,Double>>(simUsers.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(simUsersList,new Comparator<Map.Entry<Integer,Double>>() {
            //降序排序
            public int compare(Entry<Integer, Double> o1,
                    Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        
		//初始化前十个最相近的用户的观影纪录
        int count = 0;
		for(int i = 0 ; i < simUsersList.size() ; i++){
			//System.out.println(e.getKey() + " " +e.getValue());
			if(count >= simUserNum){//找到最相近simUserNum个退出
				break;
			}
			sql = "select * from rating_meta_data where uid='" + simUsersList.get(i).getKey() + "'";
			rs = db.select(sql);
			List<Score> stemp = new ArrayList<Score>();
			while(rs.next()){
				Score s = new Score();
				s.setMid(rs.getInt("mid"));
				s.setUid(rs.getInt("uid"));
				s.setRating(rs.getInt("rating"));
				s.setTimestamp(rs.getString("timestamp"));
				stemp.add(s);
			}
			simUserScoring.put((int)simUsersList.get(i).getKey(), stemp);
		}
		
		//计算可以进行推荐的电影集合
		for(Entry e : simUserScoring.entrySet()){
			List<Score> stemp = new ArrayList<Score>();
			stemp = (List<Score>)e.getValue();
			for(int i = 0 ; i < stemp.size() ; i++){
				recommendMovieSet.add(stemp.get(i).getMid());
			}
		}
		sql = "select * from rating_meta_data where uid='" + line + "'";
		rs = db.select(sql);
		Set<Integer> tarS = new HashSet<Integer>();
		while(rs.next()){
			tarS.add(rs.getInt("mid"));
		}
		recommendMovieSet.retainAll(tarS);//过滤完毕
		
		//计算recommendMovieSet每一部电影对于目标推荐用户的评分
		for(Integer n : recommendMovieSet){//对于每一部电影
			double score = 0.0;
			for(Entry e : simUserScoring.entrySet()){//对于每一个相似的用户
				List<Score> stemp = new ArrayList<Score>();
				stemp = (List<Score>)e.getValue();
				for(int i = 0 ; i < stemp.size() ; i++){
					if(stemp.get(i).getMid() == n){
						score += simUsers.get(e.getKey()) * stemp.get(i).getRating();
					}
				}
			}
			recommendResultList.put(n, score);
		}
		
		
		//这里将map.entrySet()转换成list
        List<Map.Entry<Integer,Double>> slist = new ArrayList<Map.Entry<Integer,Double>>(recommendResultList.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(slist,new Comparator<Map.Entry<Integer,Double>>() {
            //降序排序
            public int compare(Entry<Integer, Double> o1,
                    Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        
        //推荐列表展示
        System.out.println("推荐列表：");
        System.out.println("电影名称" + "      " + "相似系数");
        count = 0;
        for(int i = 0 ; i < slist.size() ; i++){
        	if(count >= simUserNum){
        		break;
        	}
        	sql = "select * from movie_meta_data where id='" + slist.get(i).getKey() + "'";
        	rs = db.select(sql);
        	while(rs.next()){
        		System.out.println(rs.getString("movie_name") + " " + slist.get(i).getValue());
        	}
        	count++;
        }
	}
}
