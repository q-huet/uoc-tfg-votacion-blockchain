import os
import json
import datetime

storage_path = '/home/ehuetadr/TFG/VotacionBC/backend-spring/data/storage'
output_file = '/home/ehuetadr/TFG/VotacionBC/backend-spring/data/elections-db.json'

elections = {}
vote_results = {}
blob_transactions = {}
user_votes = {}

if os.path.exists(storage_path):
    for item in os.listdir(storage_path):
        if item.startswith('election-') and os.path.isdir(os.path.join(storage_path, item)):
            election_id = item.replace('election-', '')
            # The directory name IS the election ID in the storage folder structure usually?
            # Let's check the list_dir output again.
            # list_dir: election-7bead84e/
            # So the ID is likely 7bead84e or election-7bead84e.
            # In createMockElections, IDs are "election-001".
            # So the ID is likely the full folder name "election-7bead84e".
            
            election_id = item 
            
            # Count blobs
            blobs = [f for f in os.listdir(os.path.join(storage_path, item)) if f.startswith('BLOB-')]
            total_votes = len(blobs)
            
            # Create dummy election
            elections[election_id] = {
                "id": election_id,
                "title": f"Recovered Election {election_id}",
                "description": "Automatically recovered from storage data.",
                "options": [
                    {
                        "optionId": "opt1",
                        "title": "Option 1 (Recovered)",
                        "description": "Recovered option",
                        "displayOrder": 1
                    },
                    {
                        "optionId": "opt2",
                        "title": "Option 2 (Recovered)",
                        "description": "Recovered option",
                        "displayOrder": 2
                    }
                ],
                "status": "CLOSED",
                "startTime": (datetime.datetime.now() - datetime.timedelta(days=30)).isoformat(),
                "endTime": (datetime.datetime.now() - datetime.timedelta(days=1)).isoformat(),
                "createdBy": "system-recovery",
                "createdAt": (datetime.datetime.now() - datetime.timedelta(days=31)).isoformat(),
                "totalVotes": total_votes,
                "maxVotesPerUser": 1,
                "allowVoteModification": False,
                "requireAuditTrail": True,
                "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..." # Dummy key
            }
            
            # Initialize empty results
            vote_results[election_id] = {}

data = {
    "elections": elections,
    "userVotes": user_votes,
    "voteResults": vote_results,
    "blobTransactions": blob_transactions
}

with open(output_file, 'w') as f:
    json.dump(data, f, indent=2)

print(f"Generated {output_file} with {len(elections)} elections.")
