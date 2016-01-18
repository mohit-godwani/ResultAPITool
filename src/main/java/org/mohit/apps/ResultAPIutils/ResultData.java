package org.mohit.apps.ResultAPIutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.mohit.apps.nodes.*;

public class ResultData {
	
	//Caches to prevent unnecessary fetches of data.
	private static String resultHTML;
	private static String resultPageKey;
	static {
		resultHTML = "--";
		resultPageKey = "-1";
	}
	/**
	 * This method is a private method which fetches the HTML page of
	 * the requested Scholar number for the given semester
	 * @param scholar_no	Scholar number of student whose result is to be obtained
	 * @param semester	Semester for which result is to be obtained
	 * @return	Student Result HTML Page as a String
	 * @throws IOException
	 */
	private static String getResultHTML(String scholar_no, int semester) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("scholar", scholar_no);
		params.put("semester", semester);
		
		try {
			URL url = new URL("http://dolphintechnologies.in/manit/accessview.php");
			
			StringBuilder postData = new StringBuilder();
		    for (Map.Entry<String,Object> param : params.entrySet()) {
		        if (postData.length() != 0) postData.append('&');
		        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		        postData.append('=');
		        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		    }
		    String urlParameters = postData.toString();
		    URLConnection conn = url.openConnection();
	
		    conn.setDoOutput(true);
	
		    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
	
		    writer.write(urlParameters);
		    writer.flush();
	
		    StringBuilder result = new StringBuilder("");
		    String line;
		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	
		    while ((line = reader.readLine()) != null) {
		        result.append(line);
		    }
		    writer.close();
		    reader.close();
		    return result.toString();
		    
		} catch(IOException e) {
			return null;
		} 
	}
	
	private static boolean isOK(String resultHTML) {
		if (resultHTML == null)
			return false;
		if (resultHTML.contains("not found"))
			return false;
		return true;
	}
	
	/**
	 * This method checks if the result of student is available on the website or not.
	 * @param scholar_no Scholar Number of Student
	 * @param semester	Semester for which result is required
	 * @return true if Student's result is available, false otherwise
	 * @throws IOException
	 */
	public static boolean isStudentAvailable(String scholar_no, int semester) throws IOException {
		String key = scholar_no + "_" + semester;
		String resultPage;
		if (resultPageKey.equals(key)) {
			resultPage = resultHTML;
		}
		else {
			resultPage = getResultHTML(scholar_no, semester);
		}
		
		if(!isOK(resultPage))
			return false;
		return true;
	}
	
	/**
	 * This method returns student details in the form of a map
	 * @param scholar_no	Scholar number of concerned student
	 * @param semester	Semester in which student is enrolled
	 * @return Details Map
	 * Keys for the returned map are : 
	 * SCHOLAR_NO
	 * RESULT
	 * STATUS
	 * NAME
	 * SEMESTER
	 * SGPA
	 * CGPA
	 * BRANCH
	 * YEAR
	 * COURSE
	 * @throws Exception
	 */
	public static Map<String, String> getResultData(String scholar_no, int semester) throws Exception {
		Map<String, String> data = new HashMap<String, String>();
		
		String key = scholar_no + "_" + semester;
		String resultPage;
		if (resultPageKey.equals(key)) {
			resultPage = resultHTML;
		}
		else {
			resultPage = getResultHTML(scholar_no, semester);
		}
		
		if(!isOK(resultPage))
			return null;
		
		Document doc = Jsoup.parse(resultPage);
		Elements rows = doc.select("tr");
		
		String sno = rows.get(7).text().split(":")[1].trim();

		Elements detail_spans = rows.get(6).select("span");
		String name = detail_spans.get(1).text();
		String status = detail_spans.get(3).text();
		
		detail_spans = rows.get(5).select("span");
		String branch = detail_spans.get(1).text();
		String year = detail_spans.get(3).text();
		
		detail_spans = rows.get(4).select("span");
		String course = detail_spans.get(1).text();
		String sem = detail_spans.get(3).text();
		
		Elements grade_spans = rows.get(rows.size() - 4).select("span");
		String sgpa = grade_spans.get(1).text();
		sgpa = sgpa.substring(sgpa.indexOf(":") + 1).trim();
		String cgpa = grade_spans.get(3).text();
		cgpa = cgpa.substring(cgpa.indexOf(":") + 1).trim();
		
		String result = rows.get(12).text();
		result = result.substring(result.indexOf(":") + 1).trim();
		
		data.put("SCHOLAR_NO", sno);
		data.put("NAME", name);
		data.put("STATUS", status);
		data.put("SGPA", sgpa);
		data.put("CGPA", cgpa);
		data.put("BRANCH", branch);
		data.put("YEAR", year);
		data.put("COURSE", course);
		data.put("SEMESTER", sem);
		data.put("RESULT", result);
		
		/*for (Map.Entry<String, String> s : data.entrySet()) {
			System.out.println(s.getKey() + " -> " + s.getValue());
		}*/
		ResultData.resultHTML = resultPage;
		ResultData.resultPageKey = scholar_no + "_" + semester;
		return data;
	}
	
	private static Map<Subject, Grade> getSubjectwiseGrades(Document doc, String key) {
		
		Map<Subject, Grade> subjects = new HashMap<Subject, Grade>();
		
		Elements rows = doc.select("tr");
		Elements row_data = rows.get(10).select("td");
		
		String[] codes 		= row_data.get(0).select("span").html().split("<br>");
		String[] names 		= row_data.get(1).select("span").html().split("<br>");
		String[] credits	= row_data.get(2).select("span").html().split("<br>");
		for (int i = 0; i < credits.length; i ++) {
			credits[i] = credits[i].substring(credits[i].indexOf(">") + 1).trim();
		}
		String[] earnedCreadits = row_data.get(3).select("span").html().split("<br>");
		String[] CPI 			= row_data.get(4).select("span").html().split("<br>");
		String[] letterGrade 	= row_data.get(5).select("span").html().split("<br>");
		for (int i = 1; i < codes.length; i += 2) {
			Subject myKey = new Subject(names[i], credits[i], codes[i]);
			Grade value = new Grade(Double.parseDouble(CPI[i]), letterGrade[i], earnedCreadits[i]);
			subjects.put(myKey, value);
		}
		return subjects;
	}
	
	/**
	 * This method returns the subject-grade map for the requested student
	 * @param scholar_no	Scholar number of student whose result is to be obtained
	 * @param semester	Semester for which result is to be obtained
	 * @return Subject-Grade Map
	 * @throws IOException
	 */
	public static Map<Subject, Grade> getSubjectwiseGrades(String scholar_no, int semester) throws IOException {
		String key = scholar_no + "_" + semester;
		
		String resultPage;
		if (resultPageKey.equals(key)) {
			resultPage = resultHTML;
		}
		else {
			resultPage = getResultHTML(scholar_no, semester);
		}
		
		if(!isOK(resultPage))
			return null;
		
		Document doc = Jsoup.parse(resultPage);
		return getSubjectwiseGrades(doc, key);
	}
	
	/**
	 * This method helps the Client to fetch image by providing a URL of image
	 * in the form of a java.lang.String.
	 * @param scholar_no	Scholar number of student whose image is required
	 * @return String URL for image fetch
	 * @throws IOException
	 */
	public static String getImageUrlString(String scholar_no) throws IOException {
		return "http://manit.ecampuserp.com/assets/img/students/" + scholar_no + ".jpg";
	}
	
	/**
	 * This method fetches the list of braches for which result has been declared.
	 * @return	List containing branches for which result has been declared 
	 * @throws IOException
	 */
	public static List<String> getResultDeclaredBranches() throws IOException {
		URL url = new URL("http://dolphintechnologies.in/manit/results.html");
		URLConnection uc = url.openConnection();
		BufferedReader br = new BufferedReader (new InputStreamReader(uc.getInputStream()));
		
		StringBuilder result = new StringBuilder("");
		String line;
		while ((line = br.readLine()) != null) {
	        result.append(line);
	    }
		br.close();
		String finalResult = result.toString();
		
		Document doc = Jsoup.parse(finalResult);
		Elements elements = doc.select("marquee");
		Elements text = elements.get(0).select("p");
		List<String> resultList = new ArrayList<String>();
		for (Element x : text) {
			String s = x.text().trim();
			if (!s.equals(" ")) {
				System.out.println(s);
				resultList.add(s);
			}
		}
		return resultList;
	}
	/**
	 * Tester method for this class.
	 */
	/*public static void main(String[] args) throws Exception {
		String scholar_no = "121114004";
		int semester = 7;
		Map<String, String> data 	= ResultData.getResultData(scholar_no, semester);
		Map<Subject, Grade> grades 	= ResultData.getSubjectwiseGrades(scholar_no, semester);
		
		for (Map.Entry<String, String> datum : data.entrySet()) {
			System.out.println(datum.getKey() + "\t->\t" + datum.getValue());
		}
		
		for (Map.Entry<Subject, Grade> grade : grades.entrySet()) {
			System.out.println(grade.getKey().getName() + "\t->\t" + grade.getValue().getCPI());
		}
		ResultData.getResultDeclaredBranches();
	}*/
}
