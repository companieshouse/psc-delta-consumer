package uk.gov.companieshouse.psc.delta.mapper;

import java.util.List;
// no additional imports required

public enum CompanyStatus {

    ACTIVE(List.of("0", "5", "Q", "AA", "AB"), "active"),
    DISSOLVED(List.of("1", "R"), "dissolved"),
    CONVERTED_CLOSED(List.of("4", "7", "X", "Z"), "converted-closed"),
    LIQUIDATION(List.of("2"), "liquidation"),
    RECEIVERSHIP(List.of("3", "A", "F", "G"), "receivership"),
    OPEN(List.of("8"), "open"),
    CLOSED(List.of("9"), "closed"),
    INSOLVENCY_PROCEEDINGS(List.of("C", "E", "H", "J", "K", "L", "N", "O", "P", "S", "U", "V", "W"),
            "insolvency-proceedings"),
    VOLUNTARY_ARRANGEMENT(List.of("I"), "voluntary-arrangement"),
    ADMINISTRATION(List.of("M", "T"), "administration"),
    REMOVED(List.of("AD"), "removed"),
    REGISTERED(List.of("AC"), "registered");

    private final List<String> keys;
    private final String value;

    CompanyStatus(List<String> keys, String value) {
        this.keys = keys;
        this.value = value;
    }

    public static String statusFromKey(String inputKey) {
        if (inputKey == null) return null;
        String key = inputKey.trim();
        if (key.isEmpty()) return null;
        for (CompanyStatus cs : values()) {
            if (cs.keys.contains(key)) {
                return cs.value;
            }
        }
        return null;
    }

    public List<String> getKeys() {
        return keys;
    }

    public String getValue() {
        return value;
    }
}


