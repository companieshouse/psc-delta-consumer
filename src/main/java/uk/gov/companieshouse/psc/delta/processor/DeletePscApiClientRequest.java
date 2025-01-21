package uk.gov.companieshouse.psc.delta.processor;

public class DeletePscApiClientRequest {

    private final String contextId;
    private final String notificationId;
    private final String companyNumber;
    private final String deltaAt;
    private final String kind;

    private DeletePscApiClientRequest(Builder builder) {
        contextId = builder.contextId;
        notificationId = builder.notificationId;
        companyNumber = builder.companyNumber;
        deltaAt = builder.deltaAt;
        kind = builder.kind;
    }

    public String getContextId() {
        return contextId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public String getKind() {
        return kind;
    }

    public static final class Builder {

        private String contextId;
        private String notificationId;
        private String companyNumber;
        private String deltaAt;
        private String kind;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public DeletePscApiClientRequest build() {
            return new DeletePscApiClientRequest(this);
        }
    }
}
