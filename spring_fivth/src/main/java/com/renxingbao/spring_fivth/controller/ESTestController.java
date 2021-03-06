package com.renxingbao.spring_fivth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.renxingbao.spring_fivth.domain.ResponseBean;
import com.renxingbao.spring_fivth.domain.Users;
import com.renxingbao.spring_fivth.util.ESUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: cl
 * @Date: 2020/08/21
 */
@Api(value = "ES????????????????????????b", tags = {"ES????????????"})
@RestController
@RequestMapping("/es")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
@Slf4j
public class ESTestController {

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    ESUtil esUtil;

    @ApiOperation(value = "es????????????????????????sasda", notes = "es????????????????????????")
    @RequestMapping(value = "/create/index", method = RequestMethod.POST)
    public ResponseBean createIndex(@RequestParam String indexName) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("properties")
                    //.field("yml")
                    .startObject()
                    .field("name").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("age").startObject().field("index", "true").field("type", "integer").endObject()
                    .field("money").startObject().field("index", "true").field("type", "double").endObject()
                    .field("address").startObject().field("index", "true").field("type", "text").field("analyzer", "ik_max_word").endObject()
                    .field("birthday").startObject().field("index", "true").field("type", "date").field("format", "strict_date_optional_time||epoch_millis").endObject()
                    .endObject()
                    .endObject();
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.mapping(builder);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            if (acknowledged) {
                return new ResponseBean(200, "????????????s", null);
            } else {
                return new ResponseBean(1002, "????????????", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ApiOperation(value = "es??????????????????????????????", notes = "es??????????????????????????????")
    @RequestMapping(value = "/index/exists", method = RequestMethod.POST)
    public ResponseBean indexExists(@RequestParam String indexName) {
        boolean isExists = esUtil.isIndexExists(indexName);
        return new ResponseBean(200, "????????????", isExists);
    }

    @ApiOperation(value = "es????????????????????????", notes = "es????????????????????????")
    @RequestMapping(value = "/delete/index", method = RequestMethod.POST)
    public ResponseBean deleteIndex(@RequestParam String indexName) {
        boolean isDelete = esUtil.deleteIndex(indexName);
        if (isDelete) {
            return new ResponseBean(200, "????????????", null);
        } else {
            return new ResponseBean(10002, "????????????", null);
        }
    }

    @ApiOperation(value = "es??????????????????", notes = "es??????????????????")
    @RequestMapping(value = "/insert/data", method = RequestMethod.POST)
    public ResponseBean findIndustryClassList(@RequestBody Users user, @RequestParam String indexName) {
        IndexRequest indexRequest = new IndexRequest(indexName);
        String userJson = JSONObject.toJSONString(user);
        indexRequest.source(userJson, XContentType.JSON);
        try {
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if (indexResponse != null) {
                String id = indexResponse.getId();
                String index = indexResponse.getIndex();
                long version = indexResponse.getVersion();
                log.info("index:{},id:{}", index, id);
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    System.out.println("??????????????????!" + index + "-" + id + "-" + version);
                    return new ResponseBean(200, "????????????", id);
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    System.out.println("??????????????????!");
                    return new ResponseBean(10001, "????????????", null);
                }
                // ??????????????????
                ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                    System.out.println("??????????????????.....");
                }
                // ????????????????????????????????????????????????????????????
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                        String reason = failure.reason();
                        System.out.println("?????????????????????" + reason);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @ApiOperation(value = "es????????????????????????", notes = "es????????????????????????")
    @RequestMapping(value = "/query/data", method = RequestMethod.GET)
    public ResponseBean testESFind() {
        SearchRequest searchRequest = new SearchRequest("wcnm");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //?????????name??????????????????????????????name????????????????????????????????????(????????????)????????????name.keyword?????????????????????????????????????????????????????????
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("birthday").from("1991-01-01").to("2010-10-10").format("yyyy-MM-dd");//????????????
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name.keyword", name);//????????????
        PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery("name", "???");//????????????
//        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("name.keyword", "*???");//???????????????
//        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("name", "???");//????????????
        FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("age");//??????????????????
        fieldSortBuilder.sortMode(SortMode.MIN);//??????????????????

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(rangeQueryBuilder).must(prefixQueryBuilder);//and or  ??????
        //boolQueryBuilder.should(prefixQueryBuilder);//and or  ??????

        sourceBuilder.query(boolQueryBuilder).sort(fieldSortBuilder);//???????????????
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            JSONArray jsonArray = new JSONArray();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = JSON.parseObject(sourceAsString);
                jsonArray.add(jsonObject);
            }
            return new ResponseBean(200, "????????????", jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseBean(10001, "????????????", null);
        }
    }

    @ApiOperation(value = "es????????????????????????", notes = "es????????????????????????")
    @RequestMapping(value = "/query/agg", method = RequestMethod.GET)
    public ResponseBean testESFindAgg() {
        SearchRequest searchRequest = new SearchRequest("wcnm");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("by_age").field("age");
        sourceBuilder.aggregation(termsAggregationBuilder);

        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
            ParsedLongTerms parsedLongTerms = (ParsedLongTerms) stringAggregationMap.get("by_age");
            List<? extends Terms.Bucket> buckets = parsedLongTerms.getBuckets();
            Map<Integer, Long> map = new HashMap<>();
            for (Terms.Bucket bucket : buckets) {
                long docCount = bucket.getDocCount();//??????
                Number keyAsNumber = bucket.getKeyAsNumber();//??????
                System.err.println(keyAsNumber + "?????????" + docCount + "???");
                map.put(keyAsNumber.intValue(), docCount);
            }
            return new ResponseBean(200, "????????????", map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @ApiOperation(value = "es??????????????????????????????", notes = "es??????????????????????????????")
    @RequestMapping(value = "/query/address", method = RequestMethod.GET)
    public ResponseBean testESFindAddress() {
        SearchRequest searchRequest = new SearchRequest("fci3");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("by_address").field("address");
        sourceBuilder.aggregation(termsAggregationBuilder);

        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
            ParsedStringTerms parsedLongTerms = (ParsedStringTerms) stringAggregationMap.get("by_address");
            List<? extends Terms.Bucket> buckets = parsedLongTerms.getBuckets();
            Map<String, Long> map = new HashMap<>();
            for (Terms.Bucket bucket : buckets) {
                long docCount = bucket.getDocCount();//??????
                String keyAsNumber = bucket.getKeyAsString();//??????
                System.err.println(keyAsNumber + "?????????" + docCount + "???");
                map.put(keyAsNumber, docCount);
            }
            return new ResponseBean(200, "????????????", map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @ApiOperation(value = "es???????????? ???????????????????????????", notes = "es??????????????????????????????")
    @RequestMapping(value = "/query/laji", method = RequestMethod.GET)
    public ResponseBean testESFindAddresswcnm() throws IOException {
        try {

    SearchRequest searchRequest = new SearchRequest("wcnm");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    //??????????????????????????????
    // searchSourceBuilder.query(QueryBuilders.termQuery("address","??????")).size(100);
    TermsAggregationBuilder field = AggregationBuilders.terms("address").field("teamName.keyword").size(200);
    AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("age").field("age");
    TermsAggregationBuilder termsAggregationBuilder = field.subAggregation(avgAggregationBuilder);
            searchSourceBuilder.aggregation(termsAggregationBuilder);
            searchRequest.source(searchSourceBuilder);
    SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    //????????????????????????????????????
    SearchHit[] hits = search.getHits().getHits();
    List<Users> playerList = new LinkedList<>();
            for(SearchHit hit:hits) {
        Users player = JSONObject.parseObject(hit.getSourceAsString(), Users.class);
        player.setId((hit.getId()));
        playerList.add(player);
        System.out.println(player.toString());
    }

    //????????????????????????
    Aggregations aggregations = search.getAggregations();
            for(
    Aggregation a:aggregations)

    {
        Terms terms = (Terms) a;
        for (Terms.Bucket bucket : terms.getBuckets()) {
            System.out.println("key is " + bucket.getKeyAsString());
            Avg age = (Avg) bucket.getAggregations().asMap().get("age");
            double value = age.getValue();
            System.out.println("Avg age is " + value);
        }
        return new ResponseBean(200, "????????????", playerList);
      }
    }catch (IOException e) {
        e.printStackTrace();
    }
        return null;
    }








    @ApiOperation(value = "es??????????????????", notes = "es??????????????????")
    @RequestMapping(value = "/update/data", method = RequestMethod.GET)
    public ResponseBean testESUpdate(@RequestParam String id, @RequestParam Double money) {
        UpdateRequest updateRequest = new UpdateRequest("test_es", id);
        Map<String, Object> map = new HashMap<>();
        map.put("money", money);
        updateRequest.doc(map);
        try {
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                return new ResponseBean(200, "????????????", null);
            } else {
                return new ResponseBean(10002, "????????????", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseBean(1003, "????????????", null);
        }
    }

    @ApiOperation(value = "es??????????????????", notes = "es??????????????????")
    @RequestMapping(value = "/delete/data", method = RequestMethod.GET)
    public ResponseBean testESDelete(@RequestParam String id, @RequestParam String indexName) {
        DeleteRequest deleteRequest = new DeleteRequest(indexName);
        deleteRequest.id(id);
        try {
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                return new ResponseBean(1001, "????????????", null);
            } else {
                return new ResponseBean(200, "????????????", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseBean(1003, "????????????", null);
        }
    }
}
