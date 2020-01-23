# EVoting Website
This is an electronic voting website providing privacy, soundness, auditability, and unlinkability.

A user can login using a google account and have two direct options. A user can create an election or join an election with a given code. Once an election is created, the Paillier cipher generates a pair of keys to allow for encryption and decryption of the vote. The owner of the election can then end the voting and tally the votes in a publically verifiable manner. The votes are passed through a mixnet and a zero knowledge proof is generated. 

The Voting Process

![The Voting Process](https://i.imgur.com/yS1SbzF.png)
