package letscode;

import com.google.gson.Gson;

import java.sql.*;
import java.util.HashMap;

import static spark.Spark.get;
import static spark.Spark.port;


class ThreadCafe implements Runnable{
    public void run(){
        try (Connection db = DriverManager.getConnection("jdbc:h2:~/test:")) {
            try (Statement dataQuery = db.createStatement()){
                dataQuery.execute("DELETE FORM VISITS");
                for(int day = 1; day <= 100; day++){
                    for(int cafenum = 1; cafenum <= 5; cafenum++){
                        int customerCount = 0;
                        for (int j = 0; j < 20; j++){
                            customerCount += Math.random() * 10;
                        }
                        String query = "INSERT INTO VISITS VALUES('cafe_" + cafenum + "', " + customerCount +", " + day + " )";
                        dataQuery.execute(query);

                        try{
                            sleep(3000);
                        } catch(Exception e){}
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Database connection failure: "
                    + ex.getMessage());
        }
    }
}

class ThreadJson implements Runnable {
    public static run(){
        Gson gson = new Gson();
        HashMap<String, String> mapCafe = new HashMap<>();
        HashMap<String, Integer> mapAvg = new HashMap<>();

        try (Connection db = DriverManager.getConnection("jdbc:h2:~/test:")) {
            try (Statement dataQuery = db.createStatement()) {
                for (int index = 0; index < 500; index++) {
                    for (int cafenum = 1; cafenum <= 5; cafenum++) {
                        String query = "SELECT AVG(COUNTCLIENTS)as avg FROM VISITS WHERE CAFENAME = 'cafe_" + cafenum + "'";
                        if(dataQuery.excute(query)) {
                            ResultSet rs = dataQuery.executeQuery(query);
                            mapCafe.put("name", "cafe_" + cafenum);
                            gson.toJson(mapCafe);
                            mapCafe.clear();
                            while (rs.next()) {
                                int avg = rs.getInt("avg");
                                mapAvg.put("traffic_avg_day", avg);
                                gson.toJson(mapAvg);
                                mapAvg.clear();
                            }
                        }else{
                            break;
                        }
                    }
                    String rezult = gson.fromJson();
                    get("/getTrafficAvg", (req, res) -> rezult);
                    try{
                        sleep(3000);
                    } catch(Exception e){}
                }
            }
        } catch (SQLException ex) {
            System.out.println("Database connection failure: "
                + ex.getMessage());
        }
        return null;
    }
}
public class Cafe {

    public static void main(String[] args) {
        port(8090);
        Thread cafeStart = new Thread(new ThreadCafe());
        cafeStart.start();
        Thread json = new Thread(new ThreadJson());
        json.start();
    }
}
