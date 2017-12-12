package com.howell.datetime;

import android.util.Log;
import android.view.View;


import com.howell.pdcstation.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.howell.datetime.WheelDate;
import com.howell.datetime.WheelDate;


public class WheelMain {

	private View view;
	private String country;
	private WheelView wv_year;
	private WheelView wv_month;
	private WheelView wv_day;
	private WheelView wv_hours;
	private WheelView wv_mins;
	private WheelView wv_sec;
	public int screenheight;
	private static int START_YEAR = 1990, END_YEAR = 2100;

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public static int getSTART_YEAR() {
		return START_YEAR;
	}

	public static void setSTART_YEAR(int sTART_YEAR) {
		START_YEAR = sTART_YEAR;
	}

	public static int getEND_YEAR() {
		return END_YEAR;
	}

	public static void setEND_YEAR(int eND_YEAR) {
		END_YEAR = eND_YEAR;
	}

	public WheelMain(View view,String country) {
		super();
		this.country = country;
		this.view = view;
		setView(view);
	}

	/**
	 * @Description: TODO 弹出日期时间选择�?
	 */
	public void initDateTimePicker(int year ,int month ,int day) {
//		int year = calendar.get(Calendar.YEAR);
//		int month = calendar.get(Calendar.MONTH);
//		int day = calendar.get(Calendar.DATE);

		// 添加大小月月份并将其转换为list,方便之后的判�?
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		// �?
		wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"�?的显示数�?
		wv_year.setCyclic(true);// 可循环滚�?
		if(country.equals("CN"))
			wv_year.setLabel("年");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数�?

		// �?
		wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		if(country.equals("CN"))
			wv_month.setLabel("月");
		wv_month.setCurrentItem(month);

		// �?
		wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"�?的数�?
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		if(country.equals("CN"))
			wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);

		//时 分 秒
		view.findViewById(R.id.hms).setVisibility(View.GONE);
		wv_hours = view.findViewById(R.id.hour);
		wv_hours.setCurrentItem(0);
		wv_mins = view.findViewById(R.id.minute);
		wv_mins.setCurrentItem(0);
		wv_sec = view.findViewById(R.id.second);
		wv_sec.setCurrentItem(0);
		// 添加"�?监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"�?的数�?
				if (list_big
						.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month
						.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0)
							|| year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// 添加"�?监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"�?的数�?
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0 && (wv_year
							.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);

		// 根据屏幕密度来指定�?择器字体的大�?不同屏幕可能不同)
		int textSize = 0;
		textSize = (screenheight / 100) * 4;
		wv_day.setTEXT_SIZE(textSize);
		wv_month.setTEXT_SIZE(textSize);
		wv_year.setTEXT_SIZE(textSize);

	}

	public void initDateTimePicker(int year , int month , int day, int hour, int min, int sec, final OnMyWheelChangeListener o) {
//		int year = calendar.get(Calendar.YEAR);
//		int month = calendar.get(Calendar.MONTH);
//		int day = calendar.get(Calendar.DATE);

		// 添加大小月月份并将其转换为list,方便之后的判�?
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		// �?
		wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"�?的显示数�?
		wv_year.setCyclic(true);// 可循环滚�?
		if(country.equals("CN"))
			wv_year.setLabel("年");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数�?

		// �?
		wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		if(country.equals("CN"))
			wv_month.setLabel("月");
		wv_month.setCurrentItem(month);

		// �?
		wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"�?的数�?
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		if(country.equals("CN"))
			wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);

		//时 分 秒
		wv_hours = view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));// 设置"�?的显示数�?
		wv_hours.setCyclic(true);// 可循环滚�?
		if(country.equals("CN")) wv_hours.setLabel("时");// 添加文字
		wv_hours.setCurrentItem(hour);// 初始化时显示的数�?

		wv_mins = view.findViewById(R.id.minute);
		wv_mins.setAdapter(new NumericWheelAdapter(0,59));
		wv_mins.setCyclic(true);
		if (country.equals("CN")) wv_mins.setLabel("分");
		wv_mins.setCurrentItem(min);

		wv_sec = view.findViewById(R.id.second);
		wv_sec.setAdapter(new NumericWheelAdapter(0,59));
		wv_sec.setCyclic(true);
		if (country.equals("CN")) wv_sec.setLabel("秒");
		wv_sec.setCurrentItem(sec);

		// 添加"�?监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"�?的数�?
				if (list_big
						.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month
						.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0)
							|| year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// 添加"�?监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"�?的数�?
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0 && (wv_year
							.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}

			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);

		// 根据屏幕密度来指定�?择器字体的大�?不同屏幕可能不同)
		int textSize = 0;
		textSize = (screenheight / 100) * 3;
		wv_day.setTEXT_SIZE(textSize);
		wv_month.setTEXT_SIZE(textSize);
		wv_year.setTEXT_SIZE(textSize);
		wv_hours.setTEXT_SIZE(textSize);
		wv_mins.setTEXT_SIZE(textSize);
		wv_sec.setTEXT_SIZE(textSize);

		//send call back to activity
		OnWheelChangedListener wheelListenerAll = new OnWheelChangedListener(){

			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (o==null)return;
				switch (wheel.getId()){
					case R.id.year:
						o.onChange(newValue+START_YEAR, WheelDate.YEAR);
						break;
					case R.id.month:
						o.onChange(newValue + 1, WheelDate.MONTH);
						break;
					case R.id.day:
						o.onChange(newValue+1, WheelDate.DAY);
						break;
					case R.id.hour:
						o.onChange(newValue, WheelDate.HOUR);
						break;
					case R.id.minute:
						o.onChange(newValue, WheelDate.MIN);
						break;
					case R.id.second:
						o.onChange(newValue, WheelDate.SEC);
						break;
				}
			}
		};
		wv_year.addChangingListener(wheelListenerAll);
		wv_month.addChangingListener(wheelListenerAll);
		wv_day.addChangingListener(wheelListenerAll);
		wv_hours.addChangingListener(wheelListenerAll);
		wv_mins.addChangingListener(wheelListenerAll);
		wv_sec.addChangingListener(wheelListenerAll);
	}



	public String getEndTime() {
		String sb = new String();
		int year = (wv_year.getCurrentItem() + START_YEAR);
		int month = wv_month.getCurrentItem() + 1;
		int day = wv_day.getCurrentItem() + 1;
		String strYear = "",strMonth = "",strDay = "";
		if( year < 10 ){
			strYear = "0" + year;
		}else{
			strYear = "" + year;
		}
		if( month < 10 ){
			strMonth = "0" + month;
		}else{
			strMonth = "" + month;
		}
		if( day < 10 ){
			strDay = "0" + day;
		}else{
			strDay = "" + day;
		}
		sb=strYear+"-"
				+strMonth+"-"
				+strDay+" "
				+"23:59:59";
		return sb;
	}


	public String getStartTIme(String endTime,int reduceDays){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date=null;
		try {
			date = sdf.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE,-reduceDays);
		return sdf.format(c.getTime());
	}

	public Date getTime(){
		int year = (wv_year.getCurrentItem() + START_YEAR);
		int month = wv_month.getCurrentItem() + 1;
		int day = wv_day.getCurrentItem() + 1;
		int hour = wv_hours.getCurrentItem();
		int min = wv_mins.getCurrentItem();
		int sec = wv_sec.getCurrentItem();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR,year);
		calendar.set(Calendar.MONTH,month-1);
		calendar.set(Calendar.DAY_OF_MONTH,day);
		calendar.set(Calendar.HOUR_OF_DAY,hour);
		calendar.set(Calendar.MINUTE,min);
		calendar.set(Calendar.SECOND,sec);
		Date d = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = sdf.format(d);
		Log.i("123","year="+year+" month="+month+" day="+day+"   str="+str);
		return calendar.getTime();
	}

	 interface OnMyWheelChangeListener{

		void onChange(int v,WheelDate flag);
	}
}
