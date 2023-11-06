package uk.gov.companieshouse.psc.delta.data;

import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestData {

    public static String getCompanyDelta(String inputFile) {
        String path = "src/itest/resources/json/input/" + inputFile;
        return readFile(path);
    }

    public static String getOutputData(String outputFile) {
        String path = "src/itest/resources/json/output/" + outputFile;
        return readFile(path);
    }

    public static String getDeleteData() {
        String path = "src/itest/resources/json/input/psc_delete_delta.json";
        return readFile(path);
    }

    private static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(path)));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
