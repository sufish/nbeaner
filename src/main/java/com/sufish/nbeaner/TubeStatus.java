package com.sufish.nbeaner;

import com.sufish.nbeaner.protocol.BeanstalkResponse;

import java.util.HashMap;
import java.util.Scanner;

public class TubeStatus {
    private HashMap<String, String> values = new HashMap<>();

    public TubeStatus(BeanstalkResponse response) {
        buildFromResponse(response);
    }

    private void buildFromResponse(BeanstalkResponse response) {
        Scanner scanner = new Scanner(new String(response.getResponseData()));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parm = line.split(":");
            if (parm.length == 2) {
                values.put(parm[0].trim(), parm[1].trim());
            }
        }
    }

    public int totalJobs() {
        return Integer.valueOf(values.get("total-jobs"));
    }

    public int currentReadyJobs() {
        return Integer.valueOf(values.get("current-jobs-ready"));
    }

    public int currentReservedJobs() {
        return Integer.valueOf(values.get("current-jobs-reserved"));
    }
}
