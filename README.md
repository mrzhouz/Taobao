# Taobao
淘宝评论爬虫 公司需要抓淘宝评论，就写了个简单的抓取
只需要输入搜索商品关键词，这里会返回网页搜索结果(按销量排序 保证较多评论)
第一页的所有商品的评论的json字符串，默认每个商品20条
SearchComment searchComment = new SearchComment();
List<String> commList = searchComment.findId("小红书");
for (String str : commList) {
			System.out.println(str);
			JSONObject parseObject = JSONObject.parseObject(str);
			JSONArray jsonArray = parseObject.getJSONArray("comments");

			if (jsonArray.size() > 0) {
				for (int j = 0; j < jsonArray.size(); j++) {
					System.out.println(jsonArray.getJSONObject(j).getString("content"));
				}
			} else {
				System.out.println("暂无评论");
			}
		}
需要多条评论
List<String> commList = searchComment.findId("小红书",size);

过程中可能会因为太频繁请求，导致没有数据返回，不懂淘宝是怎么识别的
本人频繁操作出现过连打开网页看评论都需要登录
