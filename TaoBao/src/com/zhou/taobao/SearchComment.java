package com.zhou.taobao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class SearchComment {

	List<String> nids = new ArrayList<>();

	private static final int count = Runtime.getRuntime().availableProcessors() * 3 + 2;
	public static final ExecutorService scheduledTaskFactoryExecutor = Executors.newFixedThreadPool(count,
			new ThreadFactoryTest());// 按指定工厂模式来执行的线程池;

	/** 线程工厂初始化方式二 */
	private static class ThreadFactoryTest implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("XiaoMaGuo_ThreadFactory");
			// thread.setDaemon(true); // 将用户线程变成守护线程,默认false
			return thread;
		}
	}

	private DefaultHttpClient client = new DefaultHttpClient(new PoolingClientConnectionManager());;
	private HttpResponse response;
	private HttpEntity entity;

	/**
	 * 
	 * @param keyword
	 *            搜索商品关键词
	 * @param size
	 *            获取商品的评论条数
	 * @throws IOException
	 * @throws IOException
	 */

	public List<String> findId(String keyword, int size) {
		System.out.println(keyword);
		// 按销量排序&sort=sale-desc
		List<String> conmmentJson = new ArrayList<String>();
		Document document;
		try {
			String urlAfter = "https://s.taobao.com/search?q=" + keyword + "&sort=sale-desc";
			/**
			 * 第一次请求登录页面 获得cookie 相当于在登录页面点击登录，此处在URL中 构造参数，
			 * 如果参数列表相当多的话可以使用HttpClient的方式构造参数 此处不赘述
			 */
			String urlLogin = "https://rate.taobao.com/feedRateList.htm?auctionNumId=529405854276&userNumId=369004279&currentPageNum=1&pageSize=20&rateType=&orderType=sort_weight&showContent=1&attribute=&sku=&hasSku=false&folded=0&ua=159UW5TcyMNYQwiAiwQRHhBfEF8QXtHcklnMWc%3D%7CUm5OcktwTnBKdUx2SXZOcSc%3D%7CU2xMHDJ7G2AHYg8hAS8XKwUlC1c2UDxbJV9xJ3E%3D%7CVGhXd1llXGdZZ11iW2FeYVlmUWxOcE1ySX1HeU1xRXpGe0N4THVbDQ%3D%3D%7CVWldfS0SMg47BSUZIwMtEzoHOwc8ACoQKA0oWjMLW2ZIHkg%3D%7CVmJCbEIU%7CV2lJGSQEORkjGy8PMAU8CSkVKxArCzEKPx8jHSYdPQc4DVsN%7CWGFcYUF8XGNDf0Z6WmRcZkZ8R2dZDw%3D%3D&_ksTS=1463372472404_1272&callback=jsonp_tbcrate_reviews_list";
			HttpGet post = new HttpGet(urlLogin);
			response = client.execute(post);
			entity = response.getEntity();
			CookieStore cookieStore = client.getCookieStore();
			client.setCookieStore(cookieStore);

			document = Jsoup.connect(urlAfter).get();
			String html = document.toString();

			String json = html.substring(html.indexOf("g_page_config = ") + 16, html.indexOf("g_srp_loadCss()")).trim();
			if (json.endsWith(";")) {
				json = json.substring(0, json.length() - 1);
			}
			// 解析淘宝json数据
			taobaoData(json);
			for (int i = 0; i < nids.size(); i++) {
				String nid = nids.get(i);

				String pinglunUrl = "https://rate.taobao.com/feedRateList.htm?auctionNumId=" + nid
						+ "&userNumId=2560475075&currentPageNum=1&pageSize=" + size;
				System.out.println(pinglunUrl);

				HttpGet get = new HttpGet(pinglunUrl);
				System.out.println("第" + (i + 1) + "个商品的nid=" + nid);
				Thread.sleep(2000);
				response = client.execute(get);
				entity = response.getEntity();
				String jsonobj = convertStreamToString(entity.getContent()).trim();
				String substring = jsonobj;
				if (!jsonobj.startsWith("{")) {
					substring = jsonobj.substring(1, jsonobj.length() - 1);
					// iListener.falied(goodsbean, position);
					System.out.println(substring);
					conmmentJson.add(substring);
				} else {
					System.out.println(substring);
					System.out.println("cookie过期");
					break;
					// client.cok
					// HttpGet post2 = new HttpGet(urlLogin);
					// response = client.execute(post2);
					// entity = response.getEntity();
					// CookieStore cookieStore2 =
					// client.getCookieStore();
					// client.setCookieStore(cookieStore2);
				}
				// JSONObject parseObject = JSONObject.parseObject(substring);
				// JSONArray jsonArray = parseObject.getJSONArray("comments");
				//
				// if (jsonArray.size() > 0) {
				// for (int j = 0; j < jsonArray.size(); j++) {
				//
				// System.out.println(jsonArray.getJSONObject(j).getString("content"));
				// }
				// } else {
				// System.out.println("nid=" + nid + "的暂无评论");
				// }
				// return conmmentJson;

			}

		} catch (IOException e2)

		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// Element content = document.getElementById("main");
		// Element first = document.select(".grid-left").first();
		// Elements elments = document.select(".srp-main");
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conmmentJson;

	}

	public List<String> findId(String keyword) {
		return findId(keyword, 20);
	}

	private void taobaoData(String json) {
		// TODO Auto-generated method stub

		JSONObject parseObject = JSONObject.parseObject(json);
		JSONObject mods = parseObject.getJSONObject("mods");
		JSONObject itemlist = mods.getJSONObject("itemlist");
		if (itemlist.containsKey("data")) {
			JSONArray auctions = itemlist.getJSONObject("data").getJSONArray("auctions");
			List<String> mNids = new ArrayList<>();
			for (int i = 0; i < auctions.size(); i++) {
				nids.add(auctions.getJSONObject(i).getString("nid"));
				System.out.println(auctions.getJSONObject(i).getString("nid"));
			}

		} else {
			// findId(goodsbean, position);
			System.out.println("没有搜索到商品");
		}

	}

	public static boolean isNumericl(String str) {

		Pattern pattern = Pattern.compile("[0-9]*");

		return pattern.matcher(str).matches();

	}

	/**
	 * Write htmL to file. 将请求结果以二进制形式放到文件系统中保存为.html文件,便于使用浏览器在本地打开 查看结果
	 * 
	 * @param entity
	 *            the entity
	 * @param pathName
	 *            the path name
	 * @throws Exception
	 *             the exception
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader br = null;
		StringBuffer str = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(is, "GBK"));
			String line;
			while ((line = br.readLine()) != null) {
				str.append(line);
				str.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return str.toString();
	}

}
