package com.ssh.app.service.impl;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import com.ssh.app.dao.TCheckLibraryDao;
import com.ssh.app.dao.TStudentDao;
import com.ssh.app.entity.TCheckLibrary;
import com.ssh.app.service.TCheckLibraryService;
import com.ssh.app.vo.CheckLibraryCensusVo;
import com.ssh.common.bean.Page;
import com.ssh.common.service.BaseServiceImpl;
import com.ssh.common.util.Constant;
import com.ssh.common.util.ExportDataToExcel;
import com.ssh.common.util.StringUtil;


@Service("TCheckLibraryService")
public class CopyOfTCheckLibraryServiceImpl extends BaseServiceImpl<TCheckLibrary, Long> implements TCheckLibraryService  {
	
	@Resource
	TCheckLibraryDao tCheckLibraryDao;
	@Resource
	TStudentDao tStudentDao;
	/**
	 * 通过学校、班级、年/月份得到当月所有学生的阅览室考勤统计报表
	 * @param school
	 * @param classid
	 * @param yearAndMonth
	 */
	@Override
	public List<CheckLibraryCensusVo> getCheckLibraryByMon(String school,String classid,String yearAndMonth) {
		// TODO Auto-generated method stub
		List<TCheckLibrary> tCheckLibarayList = tCheckLibraryDao.getListCheckLibrary(school,classid,yearAndMonth);
		List<Map<String, Object>> signList = new ArrayList<Map<String, Object>>();
		List<CheckLibraryCensusVo> checkLibraryCensusList = new ArrayList<CheckLibraryCensusVo>();
		for (TCheckLibrary tCheckLibrary : tCheckLibarayList) {
			String studentId =  String.valueOf(tCheckLibrary.getStudentId());
			Date checkTime =  tCheckLibrary.getCheckTime();
			String type =  String.valueOf(tCheckLibrary.getType());
			//定义签入MAP，保存用户签入的时间信息
			Map<String, Object> signin = new HashMap<String,Object>();
			//定义签出MAP，保存用户签出的时间信息
			Map<String, Object> signout = new HashMap<String,Object>();
			if(type.equalsIgnoreCase("0")){ 
				signin.put("studentId", studentId);
				signin.put("checkTime", checkTime);
				signin.put("type", type);
				signList.add(signin);
			}else{
				signout.put("studentId", studentId);
				signout.put("checkTime", checkTime);
				signout.put("type", type);
				signList.add(signout);
			}
		}
		DecimalFormat df = new DecimalFormat("0.0");
		//月总计时长
		String countInLibraryAll = "";
		double addMins = 0;
		//日总计时长
		String dayCountAll = "";
        CheckLibraryCensusVo checkLibraryCensusVo  = new CheckLibraryCensusVo();
        Map<String,String> inLibraryHoursMap = checkLibraryCensusVo.getinLibraryHoursMap();
         
        	//计算当天每个学生签入、签出时间差总和
    		for (int i = 0 ; i<signList.size();i++) {
    				//获取当前day
    				int dayOne = ((Date)signList.get(i).get("checkTime")).getDate();
    				
    				//判断当前是签入or签出，如果签入：跳出循环，同时有签入和签出才能计算时间。
    				if(signList.get(i).get("type").equals("0")){
    					continue;
    				}else{
    					//判断当前是否为最后一条记录
    					System.out.println("i===="+i);
        				/*if(i+1 >= signList.size()){
        					checkLibraryCensusVo = new CheckLibraryCensusVo();
    				        inLibraryHoursMap = checkLibraryCensusVo.getinLibraryHoursMap();
    				        inLibraryHoursMap.put("day_"+dayOne, df.format(addMins)+"");
    				        countInLibraryAll = inLibraryHoursMap.get("day_all");
    				        countInLibraryAll = df.format(Double.parseDouble(countInLibraryAll)+addMins);
    				        inLibraryHoursMap.put("day_all", countInLibraryAll);
    				        checkLibraryCensusVo.setinLibraryHoursMap(inLibraryHoursMap);
    				        checkLibraryCensusVo.setStudentName(tStudentDao.getStudentDetailByStudentId(signList.get(i).get("studentId").toString()).get(0).get("studentName").toString());
    				        checkLibraryCensusList.add(checkLibraryCensusVo);
        					break;
        				}*/
        				//确定签入签出是否同一天、同一时段
        				if(signList.get(i+1).get("type").equals("0") && dayOne == ((Date)signList.get(i+1).get("checkTime")).getDate()){
        					int hours = ((Date)signList.get(i).get("checkTime")).getHours();
        					int minutes = ((Date)signList.get(i).get("checkTime")).getMinutes();
        					int hoursTwo = ((Date)signList.get(i+1).get("checkTime")).getHours();
        					int minutesTwo = ((Date)signList.get(i+1).get("checkTime")).getMinutes();
        					addMins += (hours-hoursTwo)+(minutes-minutesTwo)/60.0;
        					//同一个学生同一天连续考勤
        					if(i != 0 && signList.get(i).get("studentId").equals(signList.get(i-1).get("studentId")) && dayOne == ((Date)signList.get(i-1).get("checkTime")).getDate()){
        						System.out.println(signList.get(i).get("studentId")+"同一天连续考勤");
        						inLibraryHoursMap = checkLibraryCensusVo.getinLibraryHoursMap();
        						dayCountAll = inLibraryHoursMap.get("day_"+dayOne);
        				        inLibraryHoursMap.put("day_"+dayOne, df.format(Double.parseDouble(dayCountAll)+addMins)+"");
        				        countInLibraryAll = inLibraryHoursMap.get("day_all");
        				        countInLibraryAll = df.format(Double.parseDouble(countInLibraryAll)+addMins);
        				        inLibraryHoursMap.put("day_all", countInLibraryAll);
        				        checkLibraryCensusVo.setinLibraryHoursMap(inLibraryHoursMap);
        				        checkLibraryCensusVo.setStudentName(tStudentDao.getStudentDetailByStudentId(signList.get(i).get("studentId").toString()).get(0).get("studentName").toString());
        				        if(i%2 != 0 && i+2 < signList.size()) 
        				        { 
        				        	if(!signList.get(i).get("studentId").equals(signList.get(i+2).get("studentId"))){
        				        		checkLibraryCensusList.add(checkLibraryCensusVo);
        				        	}
        				        }
        				        addMins = 0;
        					}//同一个学生非同一天连续考勤
        					else if(i != 0 && signList.get(i).get("studentId").equals(signList.get(i-1).get("studentId")) && dayOne != ((Date)signList.get(i-1).get("checkTime")).getDate()){
        						System.out.println(signList.get(i).get("studentId")+"非同一天连续考勤");
        						inLibraryHoursMap = checkLibraryCensusVo.getinLibraryHoursMap();
        						dayCountAll = inLibraryHoursMap.get("day_"+dayOne);
        				        inLibraryHoursMap.put("day_"+dayOne, df.format(Double.parseDouble(dayCountAll)+addMins)+"");
        				        countInLibraryAll = inLibraryHoursMap.get("day_all");
        				        countInLibraryAll = df.format(Double.parseDouble(countInLibraryAll)+addMins);
        				        inLibraryHoursMap.put("day_all", countInLibraryAll);
        				        checkLibraryCensusVo.setinLibraryHoursMap(inLibraryHoursMap);
        				        checkLibraryCensusVo.setStudentName(tStudentDao.getStudentDetailByStudentId(signList.get(i).get("studentId").toString()).get(0).get("studentName").toString());
        				        if(i%2 != 0 && i+2 < signList.size()) 
        				        { 
        				        	if(!signList.get(i).get("studentId").equals(signList.get(i+2).get("studentId"))){
        				        		checkLibraryCensusList.add(checkLibraryCensusVo);
        				        	}
        				        }
        				        addMins = 0;
        					}else{
        						checkLibraryCensusVo = new CheckLibraryCensusVo();
        				        inLibraryHoursMap = checkLibraryCensusVo.getinLibraryHoursMap();
        				        inLibraryHoursMap.put("day_"+dayOne, df.format(addMins)+"");
        				        countInLibraryAll = inLibraryHoursMap.get("day_all");
        				        countInLibraryAll = df.format(Double.parseDouble(countInLibraryAll)+addMins);
        				        inLibraryHoursMap.put("day_all", countInLibraryAll);
        				        checkLibraryCensusVo.setinLibraryHoursMap(inLibraryHoursMap);
        				        checkLibraryCensusVo.setStudentName(tStudentDao.getStudentDetailByStudentId(signList.get(i).get("studentId").toString()).get(0).get("studentName").toString());
        				        System.out.println("添加考勤记录"+i);
        				        checkLibraryCensusList.add(checkLibraryCensusVo);
            					addMins = 0;
        					}
        					 
        				}
    				}
    		}
    	
		return checkLibraryCensusList;
	} 
	
	/**
	 * 获取阅览室考勤所有记录
	 * @param school
	 * @param classname
	 * @param checkstarttime
	 * @param checkendtime
	 * @param studentname
	 */
	@Override
	public Page getPage(Page page, String school, String classname,
			String checkstarttime, String checkendtime, String studentname) {
		// TODO Auto-generated method stub
		StringBuffer whereSql1 = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		whereSql1.append(" WHERE 1 = 1 AND t.studentid = ts.id AND t.schoolid = tsc.code AND t.classid = tc.id ");
		
		if(StringUtil.notBlank(school)) {
			whereSql1.append(" and t.schoolId = ? ");
			params.add(school);
		}
		if(StringUtil.notBlank(classname)) {
			whereSql1.append(" and tc.name = ? ");
			params.add(classname);
		}
		if(StringUtil.notBlank(studentname)) {
			whereSql1.append(" and ts.name like ? ");
			params.add("%" + studentname + "%");
		}
		if(StringUtil.notBlank(checkstarttime)) {
			whereSql1.append(" and t.checktime >= ? ");
			params.add(checkstarttime+ " 00:00:00");
		}
		if(StringUtil.notBlank(checkendtime)) {
			whereSql1.append(" and t.checktime <= ? ");
			params.add(checkendtime + " 23:59:59");
		}
		 
		StringBuffer orderSql = new StringBuffer();
		StringBuffer groupBySql = new StringBuffer();
		groupBySql.append("GROUP BY t.id ");
		orderSql.append("ORDER BY t.ID DESC ");
		
		page = tCheckLibraryDao.findCheckPage(page, whereSql1.toString(), params, orderSql.toString(), groupBySql.toString());
		return page;
	}

	/**
	 * 将学生阅览室考勤记录导入Excel
	 * @param realPath
	 * @param schoolId
	 * @param yearAndMonth
	 * @param classid
	 * @return
	 */
	public String getCheckDataLoad (String realPath,String schoolId,String yearAndMonth,String classid) {
		String fileName = String.valueOf(System.currentTimeMillis());
		OutputStream out = null;
		try {
			List<CheckLibraryCensusVo> checkLibraryCensusList = new ArrayList<CheckLibraryCensusVo>();
			checkLibraryCensusList = this.getCheckLibraryByMon(schoolId, classid, yearAndMonth);
			String path = Constant.EXPORT_EXCEL_PATH;
			HSSFWorkbook workBook = ExportDataToExcel.createCheckLibraryDateWorkbook(checkLibraryCensusList);
			out = new FileOutputStream(realPath + File.separator + path + File.separator + fileName + ".xls");
			workBook.write(out);
		    out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileName;
	}
	
	public TCheckLibraryDao gettCheckLibraryDao() {
		return tCheckLibraryDao;
	}

	public void settCheckLibraryDao(TCheckLibraryDao tCheckLibraryDao) {
		this.tCheckLibraryDao = tCheckLibraryDao;
	}

}