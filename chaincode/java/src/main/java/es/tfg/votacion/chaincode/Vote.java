package main.java.es.tfg.votacion.chaincode;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public class Vote {

    @Property()
    private final String voteId;

    @Property()
    private final String electionId;

    @Property()
    private final String commitment;

    @Property()
    private final long timestamp;

    public Vote(@JsonProperty("voteId") final String voteId,
                @JsonProperty("electionId") final String electionId,
                @JsonProperty("commitment") final String commitment,
                @JsonProperty("timestamp") final long timestamp) {
        this.voteId = voteId;
        this.electionId = electionId;
        this.commitment = commitment;
        this.timestamp = timestamp;
    }

    public String getVoteId() {
        return voteId;
    }

    public String getElectionId() {
        return electionId;
    }

    public String getCommitment() {
        return commitment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        Vote other = (Vote) obj;
        return Objects.equals(getVoteId(), other.getVoteId()) &&
                Objects.equals(getElectionId(), other.getElectionId()) &&
                Objects.equals(getCommitment(), other.getCommitment()) &&
                Objects.equals(getTimestamp(), other.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVoteId(), getElectionId(), getCommitment(), getTimestamp());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [voteId=" + voteId + ", electionId=" + electionId + ", commitment=" + commitment + ", timestamp=" + timestamp + "]";
    }
}
