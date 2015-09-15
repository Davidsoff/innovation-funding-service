package com.worth.ifs.finance.service;

import com.worth.ifs.commons.service.BaseRestServiceProvider;
import com.worth.ifs.finance.domain.Cost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class CostRestServiceImpl extends BaseRestServiceProvider implements CostRestService {
    private final Log log = LogFactory.getLog(getClass());

    @Value("${ifs.data.service.rest.cost}")
    String costRestURL;

    public void add(Long applicationFinanceId, Long questionId) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(dataRestServiceURL + costRestURL + "/add/"+applicationFinanceId + "/" + questionId, applicationFinanceId, questionId);
    }

    public List<Cost> getCosts(Long applicationFinanceId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Cost[]> responseEntity = restTemplate.getForEntity(dataRestServiceURL + costRestURL + "/get/"+applicationFinanceId, Cost[].class);
        Cost[] costs = responseEntity.getBody();
        return Arrays.asList(costs);
    }

    public void update(Cost cost) {
        log.debug("Update cost - " + cost);
        RestTemplate restTemplate = new RestTemplate();
        String url = dataRestServiceURL + costRestURL + "/update/" +cost.getId();

        //set your entity to send
        HttpEntity<Cost> entity = new HttpEntity<>(cost, getJSONHeaders());

        log.info("ApplicationRestRestService.saveApplication send it!");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity
                , String.class);

        log.debug("done" + cost);
    }

    public void delete(Long costId) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(dataRestServiceURL + costRestURL + "/delete/"+costId);
    }

    public Cost findById(Long id) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Cost> responseEntity = restTemplate.getForEntity(dataRestServiceURL + costRestURL + "/findById/"+id, Cost.class);
        return responseEntity.getBody();
    }
}
