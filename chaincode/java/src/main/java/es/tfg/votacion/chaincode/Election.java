package main.java.es.tfg.votacion.chaincode;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public class Election {

    @Property()
    private final String electionId;

    @Property()
    private final String status; // "ACTIVE", "CLOSED"

    @Property()
    private final int totalVotes;

    @Property()
    private final String publicKey;

    public Election(@JsonProperty("electionId") final String electionId,
                    @JsonProperty("status") final String status,
                    @JsonProperty("totalVotes") final int totalVotes,
                    @JsonProperty("publicKey") final String publicKey) {
        this.electionId = electionId;
        this.status = status;
        this.totalVotes = totalVotes;
        this.publicKey = publicKey;
    }

    public String getElectionId() {
        return electionId;
    }

    public String getStatus() {
        return status;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        Election other = (Election) obj;
        return Objects.equals(getElectionId(), other.getElectionId()) &&
                Objects.equals(getStatus(), other.getStatus()) &&
                Objects.equals(getTotalVotes(), other.getTotalVotes()) &&
                Objects.equals(getPublicKey(), other.getPublicKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElectionId(), getStatus(), getTotalVotes(), getPublicKey());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [electionId=" + electionId + ", status=" + status + ", totalVotes=" + totalVotes + ", publicKey=" + publicKey + "]";
    }
}
