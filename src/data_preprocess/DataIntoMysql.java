package data_preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import model.Movie;
import model.Score;
import model.User;
import util.Jdbc_Util;

public class DataIntoMysql {
	
	
	
	public static void main(String[] args) throws IOException{
		Jdbc_Util db = new Jdbc_Util();
		File file = new File("ratings.txt");
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis,"utf-8");
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		/*while((line = br.readLine())!=null){
			String[] temp;
			temp = line.split("::");
			Movie m = new Movie();
			m.setId(Integer.parseInt(temp[0]));
			m.setMovie_name(temp[1].replaceAll("'", "."));
			m.setMovie_category(temp[2].replaceAll("'", ".").replaceAll("\\|", "&"));
			String sql = "insert into `movie_meta_data` (`id`, `movie_name`, `movie_catagory`) values ('" + m.getId() + "', '" + m.getMovie_name() + "', '" + m.getMovie_category() + "');";
			db.add(sql);
		}*/
		/*while((line = br.readLine())!=null){
			String[] temp;
			temp = line.split("::");
			User u = new User();
			u.setId(Integer.parseInt(temp[0]));
			u.setGender(temp[1].replaceAll("'", "."));
			if(temp[2].equals("1")){
				temp[2] = "1";
			}else if(temp[2].equals("18")){
				temp[2] = "2";
			}else if(temp[2].equals("25")){
				temp[2] = "3";
			}else if(temp[2].equals("35")){
				temp[2] = "4";
			}else if(temp[2].equals("45")){
				temp[2] = "5";
			}else if(temp[2].equals("50")){
				temp[2] = "6";
			}else if(temp[2].equals("56")){
				temp[2] = "7";
			}
			u.setAge(temp[2]);
			u.setWork(temp[3]);
			String sql = "insert into `user_meta_data` (`id`, `gender`, `age`, `work`) values ('" + u.getId() + "', '" + u.getGender() + "', '" + u.getAge() + "', '" + u.getWork() + "');";
			db.add(sql);
		}*/
		while((line = br.readLine())!=null){
			String[] temp;
			temp = line.split("::");
			Score s = new Score();
			s.setUid(Integer.parseInt(temp[0]));
			s.setMid(Integer.parseInt(temp[1]));
			s.setRating(Integer.parseInt(temp[2]));
			s.setTimestamp(temp[3]);
			String sql = "insert into `rating_meta_data` (`uid`, `mid`, `rating`, `timestamp`) values ('" + s.getUid() + "', '" + s.getMid() + "', '" + s.getRating() + "', '" + s.getTimestamp() + "');";
			db.add(sql);
		}
		br.close();
		isr.close();
		fis.close();
	}
}
