# Scala Jobcoin
Simple base project for the Jobcoin project using Scala and SBT. It accepts return address as arguments and prints out a deposit address to the user for them to send their funds to. The rest of the application is left unimplemented.

### Run
`sbt run`


### Test
`sbt test`

### Thinking process:

1. All coins transactions are public (Everyone can see all the transactions). This is reason of why the mixer is important.
2. The mixer will deposit the coins into different addresses associated with the main address so we can hide user transactions.

# Tasks

1. Validate the user addresses are new and unused using the API.
2. The mixer will associate the user addresses with a generated deposit address.
3. Create an async process that will be watching when a deposit is being made to the "Deposit Address."
	3.1. The money will be deposited into the house account.
	3.2 The house account will slowly deposit the coins into the addresses that user provided.
4. Collect a Fee for splitting the money.
