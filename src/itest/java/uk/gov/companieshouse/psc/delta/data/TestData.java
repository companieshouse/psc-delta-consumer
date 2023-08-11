package uk.gov.companieshouse.psc.delta.data;

import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestData {

    public static String getCompanyDelta() {
        String path = "src/itest/resources/json/input/corporate_entity_BO_psc_delta.json";
        return readFile(path);
    }

    public static String getOutputData() {
        String path = "src/itest/resources/json/output/corporate_entity_BO_psc_expected_output.json";
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
