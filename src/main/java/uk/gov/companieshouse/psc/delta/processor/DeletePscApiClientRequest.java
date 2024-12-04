package uk.gov.companieshouse.psc.delta.processor;

import java.util.Objects;
import uk.gov.companieshouse.api.delta.PscDeleteDelta.KindEnum;

public class DeletePscApiClientRequest {

    private final String contextId;
    private final String notificationId;
    private final String companyNumber;
    private final String deltaAt;
    private KindEnum kind;

    public DeletePscApiClientRequest(String contextId, String notificationId, String companyNumber, String deltaAt,
            KindEnum kind) {
        this.contextId = contextId;
        this.notificationId = notificationId;
        this.companyNumber = companyNumber;
        this.deltaAt = deltaAt;
        this.kind = kind;
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

    public KindEnum getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeletePscApiClientRequest that = (DeletePscApiClientRequest) o;
        return Objects.equals(contextId, that.contextId) && Objects.equals(notificationId,
                that.notificationId) && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(deltaAt, that.deltaAt) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(contextId);
        result = 31 * result + Objects.hashCode(notificationId);
        result = 31 * result + Objects.hashCode(companyNumber);
        result = 31 * result + Objects.hashCode(deltaAt);
        result = 31 * result + Objects.hashCode(kind);
        return result;
    }

    @Override
    public String toString() {
        return "DeletePscApiClientRequest{" +
                "contextId='" + contextId + '\'' +
                ", notificationId='" + notificationId + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", deltaAt='" + deltaAt + '\'' +
                ", kind=" + kind +
                '}';
    }
}
