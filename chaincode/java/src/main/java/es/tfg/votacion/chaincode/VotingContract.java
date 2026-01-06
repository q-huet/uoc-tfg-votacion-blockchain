package main.java.es.tfg.votacion.chaincode;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(
        name = "VotingContract",
        info = @Info(
                title = "Voting Contract",
                description = "Smart contract for blockchain voting",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "ehuet@uoc.edu",
                        name = "Enrique Huet",
                        url = "https://github.com/q-huet/uoc-tfg-votacion-blockchain")))
@Default
public class VotingContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum VotingErrors {
        ELECTION_NOT_FOUND,
        ELECTION_ALREADY_EXISTS,
        ELECTION_CLOSED,
        VOTE_ALREADY_EXISTS,
        ALREADY_VOTED
    }

    /**
     * Initialize the ledger with some sample data
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        // Initialize with no elections or some sample if needed
        System.out.println("Ledger initialized");
    }

    /**
     * Create a new election
     *
     * @param ctx        the transaction context
     * @param electionId the ID of the new election
     * @param publicKey  the public key for the election
     * @return the created election
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Election createElection(final Context ctx, final String electionId, final String publicKey) {
        ChaincodeStub stub = ctx.getStub();
        String electionState = stub.getStringState(electionId);

        if (electionState != null && !electionState.isEmpty()) {
            String errorMessage = String.format("Election %s already exists", electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VotingErrors.ELECTION_ALREADY_EXISTS.toString());
        }

        Election election = new Election(electionId, "ACTIVE", 0, publicKey);
        electionState = genson.serialize(election);
        stub.putStringState(electionId, electionState);

        return election;
    }

    /**
     * Emit a vote for an election
     *
     * @param ctx        the transaction context
     * @param electionId the ID of the election
     * @param commitment the encrypted vote hash
     * @param userId     the ID of the user voting (provided by backend)
     * @return the transaction ID
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String emitVote(final Context ctx, final String electionId, final String commitment, final String userId) {
        ChaincodeStub stub = ctx.getStub();
        String electionState = stub.getStringState(electionId);

        if (electionState == null || electionState.isEmpty()) {
            String errorMessage = String.format("Election %s does not exist", electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VotingErrors.ELECTION_NOT_FOUND.toString());
        }

        Election election = genson.deserialize(electionState, Election.class);

        if ("CLOSED".equals(election.getStatus())) {
            String errorMessage = String.format("Election %s is closed", electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VotingErrors.ELECTION_CLOSED.toString());
        }

        // Check if user has already voted
        // In a real scenario with CA, we would use ctx.getClientIdentity().getId()
        // But for this PoC with a single backend identity, we trust the backend to provide the userId
        String voteRecordKey = "vote_record_" + electionId + "_" + userId;
        
        String voteRecord = stub.getStringState(voteRecordKey);
        if (voteRecord != null && !voteRecord.isEmpty()) {
            String errorMessage = String.format("User %s has already voted in election %s", userId, electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VotingErrors.ALREADY_VOTED.toString());
        }

        // Create vote object
        String txId = stub.getTxId();
        long timestamp = stub.getTxTimestamp().toEpochMilli();
        Vote vote = new Vote(txId, electionId, commitment, timestamp);

        // Store vote with a composite key or just by txId?
        // Storing by txId is good for verification.
        // Also need to link it to election to count?
        // For simplicity in this PoC, we just store the vote by TxID.
        // And we update the election total votes.
        
        String voteState = genson.serialize(vote);
        stub.putStringState(txId, voteState);
        
        // Record that user has voted
        stub.putStringState(voteRecordKey, txId);

        // Update election total votes
        Election updatedElection = new Election(election.getElectionId(), election.getStatus(), election.getTotalVotes() + 1, election.getPublicKey());
        String updatedElectionState = genson.serialize(updatedElection);
        stub.putStringState(electionId, updatedElectionState);

        return txId;
    }

    /**
     * Close an election
     *
     * @param ctx        the transaction context
     * @param electionId the ID of the election
     * @return the updated election
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Election closeElection(final Context ctx, final String electionId) {
        ChaincodeStub stub = ctx.getStub();
        String electionState = stub.getStringState(electionId);

        if (electionState == null || electionState.isEmpty()) {
            String errorMessage = String.format("Election %s does not exist", electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VotingErrors.ELECTION_NOT_FOUND.toString());
        }

        Election election = genson.deserialize(electionState, Election.class);
        Election closedElection = new Election(election.getElectionId(), "CLOSED", election.getTotalVotes(), election.getPublicKey());
        
        String closedElectionState = genson.serialize(closedElection);
        stub.putStringState(electionId, closedElectionState);

        return closedElection;
    }

    /**
     * Get election details
     *
     * @param ctx        the transaction context
     * @param electionId the ID of the election
     * @return the election details
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Election getElection(final Context ctx, final String electionId) {
        ChaincodeStub stub = ctx.getStub();
        String electionState = stub.getStringState(electionId);

        if (electionState == null || electionState.isEmpty()) {
            String errorMessage = String.format("Election %s does not exist", electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VotingErrors.ELECTION_NOT_FOUND.toString());
        }

        return genson.deserialize(electionState, Election.class);
    }

    /**
     * Verify a vote transaction
     *
     * @param ctx  the transaction context
     * @param txId the transaction ID
     * @return true if the transaction exists and is a vote
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean verifyTransaction(final Context ctx, final String txId) {
        ChaincodeStub stub = ctx.getStub();
        String voteState = stub.getStringState(txId);

        return voteState != null && !voteState.isEmpty();
    }
    
    /**
     * Get vote details by transaction ID
     * 
     * @param ctx the transaction context
     * @param txId the transaction ID
     * @return the vote details
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Vote getVote(final Context ctx, final String txId) {
        ChaincodeStub stub = ctx.getStub();
        String voteState = stub.getStringState(txId);
        
        if (voteState == null || voteState.isEmpty()) {
             String errorMessage = String.format("Vote %s does not exist", txId);
             System.out.println(errorMessage);
             throw new ChaincodeException(errorMessage, VotingErrors.VOTE_ALREADY_EXISTS.toString());
        }
        
        return genson.deserialize(voteState, Vote.class);
    }
}
