package com.nnl.CoronavirusTracker.service;

import com.nnl.CoronavirusTracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStarts = new ArrayList<>();

    public List<LocationStats> getAllStarts() {
        return allStarts;
    }

    @PostConstruct
    @Scheduled(cron = "* * 2 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {

        List<LocationStats> newStarts = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();

        // yêu cầu truy cập link VIRUS_DATA_URL
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> httpResponse =  client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvReader);
        for (CSVRecord record: records) {

            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));

            int latestCases = (Integer.parseInt(record.get(record.size() - 1)));
            int prevDayCases = (Integer.parseInt(record.get(record.size() - 2)));
            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDiffFromPrevDay(latestCases - prevDayCases);
            // System.out.println(locationStats);
            newStarts.add(locationStats);
        }
        this.allStarts = newStarts;
    }

}
